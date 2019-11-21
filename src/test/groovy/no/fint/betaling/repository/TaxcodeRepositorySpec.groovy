package no.fint.betaling.repository

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.MvakodeResource
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources
import spock.lang.Specification

class TaxcodeRepositorySpec extends Specification {

    def 'tada'() {
        given:
        def mvaCodes = new MvakodeResources()
        def mvaCode = new MvakodeResource(kode: '25', navn: '25%', systemId: new Identifikator(identifikatorverdi: 'test'))
        mvaCodes.addResource(mvaCode)

    }
}
