package no.fint.betaling.service

import no.fint.betaling.factory.CustomerFactory
import no.fint.betaling.model.Customer
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.PersonResources
import spock.lang.Specification

class CustomerServiceSpec extends Specification {

    private CustomerService customerService
    private CacheService cacheService
    private CustomerFactory kundeFactory
    private String orgId

    void setup() {
        cacheService = Mock()
        kundeFactory = Mock()
        customerService = new CustomerService(cacheService: cacheService, kundeFactory: kundeFactory, personEndpoint: "endpoints/person")
        orgId = 'test.no'
        customerService.init()
    }

    def "Get customers returns list"() {
        given:
        def personResources = createPersonResources(1, ['Testesen'])

        when:
        List<Customer> listCustomers = customerService.getCustomers(orgId, "")

        then:
        1 * cacheService.getUpdates(_ as Class<PersonResources>, _ as String, _ as String) >> personResources
        1 * kundeFactory.toCustomer(_ as PersonResource) >> createKunde('Testesen')
        listCustomers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        given:
        def lastnames = ['Feilsen', 'Testesen', 'Rettsen']
        when:
        def listCustomers = customerService.getCustomers(orgId, 'r')

        then:
        1 * cacheService.getUpdates(_ as Class<PersonResources>, _ as String, _ as String) >> createPersonResources(3, lastnames)
        1 * kundeFactory.toCustomer(_ as PersonResource) >> createKunde('Feilsen')
        1 * kundeFactory.toCustomer(_ as PersonResource) >> createKunde('Testesen')
        1 * kundeFactory.toCustomer(_ as PersonResource) >> createKunde('Rettsen')
        listCustomers.size() == 1
        listCustomers.get(0).navn.etternavn == 'Rettsen'
    }

    private static Customer createKunde(String lastname){
        return new Customer(navn: new Personnavn(fornavn: 'Test', etternavn: lastname), fulltNavn: "${lastname}, Test")
    }

    private static PersonResources createPersonResources(int resources, List<String> lastnames) {
        def personResources = new PersonResources()
        for (int i = 0; i < resources; i++) {
            def personResource = new PersonResource(
                    navn: new Personnavn(fornavn: 'Ola', etternavn: lastnames.get(i)),
                    fodselsnummer: new Identifikator(identifikatorverdi: '12345678901')
            )
            personResources.addResource(personResource)
            personResource.addLink('self', Link.with('link.to.PersonResource'))
        }
        return personResources
    }
}
