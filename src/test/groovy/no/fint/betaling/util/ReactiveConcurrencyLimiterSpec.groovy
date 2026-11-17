package no.fint.betaling.util

import no.fint.betaling.common.util.ReactiveConcurrencyLimiter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class ReactiveConcurrencyLimiterSpec extends Specification {
    def 'independent jobs share slots and a cancelled waiter does not consume capacity'() {
        given:
        def limiter = new ReactiveConcurrencyLimiter(1)
        def gate = Sinks.one()
        def started = []
        def completed = []
        limiter.limit { started << 1; gate.asMono() }.subscribe()
        def cancelled = limiter.limit { started << 2; Mono.just(2) }.subscribe()
        limiter.limit { started << 3; Mono.just(3) }.subscribe(completed::add)

        expect:
        started == [1]

        when:
        cancelled.dispose()
        gate.tryEmitValue(1)

        then:
        started == [1, 3]
        completed == [3]
        limiter.limit { Mono.just(4) }.block(Duration.ofSeconds(1)) == 4
    }

    def 'cancelling an active operation and failing a supplier both release the slot'() {
        given:
        def limiter = new ReactiveConcurrencyLimiter(1)
        def running = limiter.limit { Mono.never() }.subscribe()
        def completed = []
        limiter.limit { Mono.just(2) }.subscribe(completed::add)

        when:
        running.dispose()

        then:
        completed == [2]

        when:
        def failure = limiter.limit { throw new IllegalStateException('Failed operation') }
                .onErrorReturn('failed').block(Duration.ofSeconds(1))

        then:
        failure == 'failed'
        limiter.limit { Mono.just(3) }.block(Duration.ofSeconds(1)) == 3
    }

    def 'a large waiting queue drains synchronous results without stack overflow'() {
        given:
        def limiter = new ReactiveConcurrencyLimiter(1)
        def gate = Sinks.one()
        def completed = new AtomicInteger()
        limiter.limit { gate.asMono() }.subscribe()
        (1..5000).each { number ->
            limiter.limit { Mono.just(number) }.subscribe { completed.incrementAndGet() }
        }

        when:
        gate.tryEmitValue(0)

        then:
        completed.get() == 5000
    }

    def 'parallel operations stay within the shared limit'() {
        given:
        def limiter = new ReactiveConcurrencyLimiter(5)
        def active = new AtomicInteger()
        def peak = new AtomicInteger()

        when:
        def results = Flux.range(0, 1000).flatMap({ number ->
            limiter.limit {
                int current = active.incrementAndGet()
                peak.accumulateAndGet(current, Math::max)
                Mono.just(number).delayElement(Duration.ofMillis(1))
                        .doOnNext { active.decrementAndGet() }
            }.subscribeOn(Schedulers.parallel())
        }, 100).collectList().block(Duration.ofSeconds(10))

        then:
        results.size() == 1000
        peak.get() <= 5
        active.get() == 0
    }
}
