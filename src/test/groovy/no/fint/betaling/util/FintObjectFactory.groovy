package no.fint.betaling.util

import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.novari.fint.model.felles.kompleksedatatyper.Personnavn
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import no.novari.fint.model.resource.utdanning.elev.*
import no.novari.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource
import no.novari.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource
import no.novari.fint.model.resource.utdanning.utdanningsprogram.SkoleResource

class FintObjectFactory {

    static SkoleResource newSchool() {
        SkoleResource resource = new SkoleResource()
        resource.setSystemId(new Identifikator(identifikatorverdi: 'HVS'))
        resource.setNavn('HVS')
        resource.setSkolenummer(new Identifikator(identifikatorverdi: '08010'))
        resource.setOrganisasjonsnummer(new Identifikator(identifikatorverdi: 'NO123456789'))
        resource.addKlasse(new Link('link.to.Klasse'))
        resource.addUndervisningsgruppe(new Link('link.to.TeachingGroup'))
        resource.addKontaktlarergruppe(new Link('link.to.ContactTeacherGroup'))
        resource.addElevforhold(new Link(verdi: 'link.to.StudentRelation'))
        resource.addSelf(new Link(verdi: 'link.to.School'))
        return resource
    }

    static KlasseResource newKlasse() {
        KlasseResource resource = new KlasseResource()
        resource.setSystemId(new Identifikator(identifikatorverdi: '1TIA'))
        resource.setNavn('1TIA')
        resource.setBeskrivelse('Basisgruppe 1TIA')
        resource.addSkolear(new Link(verdi: 'link.to.Skoleaar'))
        resource.addSkole(new Link(verdi: 'link.to.School'))
        resource.addTrinn(new Link(verdi: 'link.to.Level'))
        resource.addTermin(new Link(verdi: 'link.to.Termin'))
        resource.addUndervisningsforhold(new Link(verdi: 'link.to.Undervisningsforhold'))
        resource.addKlassemedlemskap(new Link(verdi: 'link.to.Klassemedlemskap'))
        resource.addKontaktlarergruppe(new Link(verdi: 'Link.to.Kontaktlarergruppe'))
        resource.addSelf(new Link(verdi: 'link.to.Klasse'))
        return resource
    }

    static UndervisningsgruppeResource newTeachingGroup() {
        UndervisningsgruppeResource resource = new UndervisningsgruppeResource()
        resource.setSystemId(new Identifikator(identifikatorverdi: 'YFF4106'))
        resource.setNavn('YFF4106')
        resource.setBeskrivelse('Undervisningsgruppe YFF4106')
        resource.addSkole(new Link(verdi: 'link.to.School'))
        resource.addFag(new Link(verdi: 'link.to.Subject'))
        resource.addGruppemedlemskap(new Link(verdi: 'link.to.TeachingGroupMembership'))
        resource.addSelf(new Link(verdi: 'link.to.TeachingGroup'))
        return resource
    }

    static KontaktlarergruppeResource newContactTeacherGroup() {
        KontaktlarergruppeResource resource = new KontaktlarergruppeResource()
        resource.setSystemId(new Identifikator(identifikatorverdi: '3T13DX'))
        resource.setNavn('3T13DX')
        resource.setBeskrivelse('Kontaktlærergruppe 3T13DX')
        resource.addSkole(new Link(verdi: 'link.to.School'))
        resource.addKlasse(new Link(verdi: 'link.to.Klasse'))
        resource.addGruppemedlemskap(new Link(verdi: 'link.to.ContactTeacherGroupMembership'))
        resource.addSelf(new Link(verdi: 'link.to.ContactTeacherGroup'))
        return resource
    }

    static ElevforholdResource newStudentRelation() {
        ElevforholdResource resource = new ElevforholdResource()
        resource.setSystemId(new Identifikator(identifikatorverdi: '21i3v9_HVS'))
        resource.setBeskrivelse('Elev ved HVS')
        resource.addSkole(new Link(verdi: 'link.to.School'))
        resource.addElev(new Link(verdi: 'link.to.Student'))
        resource.addSelf(new Link(verdi: 'link.to.StudentRelation'))
        return resource
    }

    static PersonResource newStudent() {
        PersonResource resource = new PersonResource()
        resource.setNavn(new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))
        resource.setFodselsnummer(new Identifikator(identifikatorverdi: '12345678901'))
        resource.setKontaktinformasjon(new Kontaktinformasjon(epostadresse: 'ola@testesen.no', mobiltelefonnummer: '30960547'))
        resource.setPostadresse(new AdresseResource(adresselinje: ['Testeveien 13'], postnummer: '1234', poststed: 'Testeby'))
        resource.addElev(new Link(verdi: 'link.to.Student'))
        resource.addSelf(new Link(verdi: 'link.to.Self'))
        return resource
    }

    static KlassemedlemskapResource newKlassemedlemskap() {
        KlassemedlemskapResource resource = new KlassemedlemskapResource()
        resource.addElevforhold(new Link(verdi: 'link.to.StudentRelation'))
        resource.addKlasse(new Link(verdi: 'link.to.Klasse'))
        resource.addSelf(new Link(verdi: 'link.to.Klassemedlemskap'))
        return resource
    }

    static KontaktlarergruppemedlemskapResource newContactTeacherGroupMembership() {
        KontaktlarergruppemedlemskapResource resource = new KontaktlarergruppemedlemskapResource()
        resource.addElevforhold(new Link(verdi: 'link.to.StudentRelation'))
        resource.addSelf(new Link(verdi: 'link.to.Self'))
        return resource
    }

    static UndervisningsgruppemedlemskapResource newTeachingGroupMembership() {
        UndervisningsgruppemedlemskapResource resource = new UndervisningsgruppemedlemskapResource()
        resource.addElevforhold(new Link(verdi: 'link.to.StudentRelation'))
        resource.addSelf(new Link(verdi: 'link.to.Self'))
        return resource
    }
}
