package no.fint.betaling.controller

import no.fint.betaling.model.Taxcode
import no.fint.betaling.taxcode.TaxcodeRepository
import no.fint.betaling.taxcode.TaxcodeController
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import spock.lang.Specification

class TaxcodeControllerSpec extends Specification {

    private TaxcodeController controller

    @SpringBean
    private TaxcodeRepository repository = Mock(TaxcodeRepository.class)

    void setup() {
        controller = new TaxcodeController(repository)
    }

    def "Get mva codes given valid org id returns list -2"() {
        given:
        def taxcode = new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)

        when:
        def response = controller.getMvaCodes()

        then:
        1 * repository.getTaxcodes() >> [taxcode]
        response.statusCode == HttpStatus.OK
        response.getBody() == [taxcode]
    }
}
