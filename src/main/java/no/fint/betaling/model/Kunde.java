package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Data
public class Kunde {
    private static final String ETTERNAVN_FORNAVN_MELLOMNAVN = "%s, %s %s";
    private static final String ETTERNAVN_FORNAVN = "%s, %s";

    private String kundenummer;
    private Personnavn navn;
    private String fulltNavn;
    private Kontaktinformasjon kontaktinformasjon;
    private AdresseResource postadresse;
    private Link person;
    private Link elev;

    public static Kunde of(PersonResource resource) {
        Kunde customer = new Kunde();
        customer.setKundenummer(getCustomerId(resource.getFodselsnummer().getIdentifikatorverdi()));
        customer.setNavn(resource.getNavn());
        customer.setFulltNavn(getPersonnavnAsString(resource.getNavn()));
        customer.setKontaktinformasjon(resource.getKontaktinformasjon());
        customer.setPostadresse(resource.getBostedsadresse());
        resource.getSelfLinks().stream().findAny().ifPresent(customer::setPerson);
        resource.getElev().stream().findAny().ifPresent(customer::setElev);
        return customer;
    }

    private static String getCustomerId(String nin) {
        try {
            return Long.toString((Long.parseLong(nin) / 100), 36);
        } catch (NumberFormatException ex) {
            return nin;
        }
    }

    private static String getPersonnavnAsString(Personnavn navn) {
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
}
