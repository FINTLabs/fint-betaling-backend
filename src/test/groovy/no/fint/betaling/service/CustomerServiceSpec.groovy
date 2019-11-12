package no.fint.betaling.service

import no.fint.betaling.util.FintObjectFactory
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

    def "Get customers returns list"() {
        given:
        def student = fintObjectFactory.newStudent()

        when:
        def customers = customerService.getCustomers(_ as String, null)

        then:
        1 * cacheService.getCache("studentCache", _ as String) >> [(student.elev.get(0)): student]
        customers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        given:
        def student = fintObjectFactory.newStudent()

        when:
        def customers = customerService.getCustomers(_ as String, 't')

        then:
        1 * cacheService.getCache("studentCache", _ as String) >> [(student.elev.get(0)): student]
        customers.size() == 1
        customers.get(0).name == 'Testesen, Ola'
    }
}
