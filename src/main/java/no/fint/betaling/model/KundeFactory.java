package no.fint.betaling.model;

import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class KundeFactory {

    private static final String ETTERNAVN_FORNAVN_MELLOMNAVN = "%s, %s %s";
    private static final String ETTERNAVN_FORNAVN = "%s, %s";

    public static String getPersonnavnAsString(Personnavn navn) {
        if (navn == null) return null;

        Optional<String> fornavn = getValue(navn.getFornavn());
        Optional<String> mellomnavn = getValue(navn.getMellomnavn());
        Optional<String> etternavn = getValue(navn.getEtternavn());

        if (fornavn.isPresent() && mellomnavn.isPresent() && etternavn.isPresent()) {
            return String.format(ETTERNAVN_FORNAVN_MELLOMNAVN, etternavn.get(), fornavn.get(), mellomnavn.get());
        } else if (fornavn.isPresent() && etternavn.isPresent()) {
            return String.format(ETTERNAVN_FORNAVN, etternavn.get(), fornavn.get());
        } else return etternavn.orElse("");
    }

    private static Optional<String> getValue(String value) {
        return StringUtils.isEmpty(value) ? Optional.empty() : Optional.of(value);
    }

    public Kunde getKunde(PersonResource person) {
        Kunde customer = new Kunde();
        customer.setKundenummer(getCustomerId(person.getFodselsnummer().getIdentifikatorverdi()));
        customer.setNavn(person.getNavn());
        customer.setFulltNavn(getPersonnavnAsString(person.getNavn()));
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());
        //person.getSelfLinks().stream().findAny().ifPresent(customer::setPerson);
        //person.getElev().stream().findAny().ifPresent(customer::setElev);
        return customer;
    }

    public static String getCustomerId(String nin) {
        try {
            return Long.toString((Long.parseLong(nin) / 100), 36);
        } catch (NumberFormatException ex) {
            return nin;
        }
    }
}