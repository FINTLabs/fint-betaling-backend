package no.fint.betaling.common.util;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** Shares a concurrency allowance between independent reactive jobs without blocking a thread. */
public final class ReactiveConcurrencyLimiter {
    private final int limit;
    private final Queue<Waiter> waiting = new ArrayDeque<>();
    private int active;
    private final AtomicInteger draining = new AtomicInteger();

    public ReactiveConcurrencyLimiter(int limit) {
        if (limit < 1) throw new IllegalArgumentException("Concurrency must be positive");
        this.limit = limit;
    }

    public <T> Mono<T> limit(Supplier<Mono<T>> operation) {
        return Mono.usingWhen(acquire(), ignored -> Mono.defer(operation),
                permit -> Mono.fromRunnable(permit::release));
    }

    private Mono<Permit> acquire() {
        return Mono.<Permit>create(sink -> {
            Waiter waiter = new Waiter(sink);
            sink.onCancel(() -> cancel(waiter));
            Permit permit = null;
            synchronized (this) {
                if (waiter.cancelled) return;
                if (active < limit) {
                    active++;
                    permit = new Permit();
                } else {
                    waiting.add(waiter);
                }
            }
            if (permit != null) sink.success(permit);
        }).doOnDiscard(Permit.class, Permit::release);
    }

    private synchronized void cancel(Waiter waiter) {
        waiter.cancelled = true;
        waiting.remove(waiter);
    }

    private void releaseNext() {
        synchronized (this) {
            active--;
        }
        // Trampoline synchronous completions so a long queue cannot overflow the stack.
        if (draining.getAndIncrement() != 0) return;
        int missed = 1;
        do {
            while (true) {
                Waiter next;
                synchronized (this) {
                    if (active == limit) break;
                    do {
                        next = waiting.poll();
                    } while (next != null && next.cancelled);
                    if (next == null) break;
                    active++;
                }
                next.sink.success(new Permit());
            }
            missed = draining.addAndGet(-missed);
        } while (missed != 0);
    }

    private static final class Waiter {
        private final MonoSink<Permit> sink;
        private boolean cancelled;

        private Waiter(MonoSink<Permit> sink) {
            this.sink = sink;
        }
    }

    private final class Permit {
        private final AtomicBoolean released = new AtomicBoolean();

        private void release() {
            if (released.compareAndSet(false, true)) releaseNext();
        }
    }
}
