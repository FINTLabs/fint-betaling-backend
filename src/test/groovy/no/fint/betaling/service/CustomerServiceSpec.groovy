package no.fint.betaling.service

import no.fint.betaling.util.FintObjectFactory
import spock.lang.Ignore
import spock.lang.Specification

class CustomerServiceSpec extends Specification {

    private CustomerService customerService
    private CacheService cacheService
    private FintObjectFactory fintObjectFactory

    void setup() {
        cacheService = Mock()
        customerService = new CustomerService(cacheService)
        fintObjectFactory = new FintObjectFactory()
    }

    @Ignore
    def "Get customers returns list"() {
        given:
        def student = fintObjectFactory.newStudent()

        when:
        def customers = customerService.getCustomers(null)

        then:
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]
        customers.size() == 1
    }

    @Ignore
    def "Get customers given filter keyword returns filtered list"() {
        given:
        def student = fintObjectFactory.newStudent()

        when:
        def customers = customerService.getCustomers('t')

        then:
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]
        customers.size() == 1
        customers.get(0).name == 'Testesen, Ola'
    }
}
