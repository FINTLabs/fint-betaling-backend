package no.fint.betaling.model

import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import spock.lang.Specification

class InvoiceFactorySpec extends Specification {

    def givenStuff(){
        def employer = new OppdragsgiverResource()
        employer.setNavn('test employer')
        employer.addLink('self', new Link('employer.self'))
        def orderLine = new VarelinjeResource()
        orderLine.setEnhet('???')
        orderLine.setKontering(new KontostrengResource())
        orderLine.setPris(100L)
        orderLine.addLink('self', new Link('orderline.self'))
    }
}


