package no.fint.betaling.controller


import no.fint.betaling.model.Principal
import no.fint.betaling.invoiceissuer.InvoiceIssuerController
import no.fint.betaling.invoiceissuer.InvoiceIssuerService
import org.spockframework.spring.SpringBean
import reactor.core.publisher.Mono
import spock.lang.Specification

class InvoiceIssuerControllerSpec extends Specification {

    @SpringBean
    InvoiceIssuerService principalService = Mock()

    InvoiceIssuerController controller = new InvoiceIssuerController(principalService)

    def "Get employers given valid org id returns list"() {
        given:
        def principal = new Principal(description: 'test')

        when:
        def response = controller.getPrincipalForSchoolId('12345')

        then:
        1 * principalService.getInvoiceIssuer('12345') >> Mono.just(principal)
        response.block() == principal
    }
}
