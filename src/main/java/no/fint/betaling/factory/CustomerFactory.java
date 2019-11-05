package no.fint.betaling.factory;

import no.fint.betaling.model.Customer;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Optional;

public enum CustomerFactory {
    ;
    private static final String SURNAME_GIVENNAME_MIDDLENAME = "%s, %s %s";
    private static final String SURNAME_GIVENNAME = "%s, %s";

    public static String getDisplayName(Personnavn navn) {
        if (navn == null) return null;

        Optional<String> fornavn = getValue(navn.getFornavn());
        Optional<String> mellomnavn = getValue(navn.getMellomnavn());
        Optional<String> etternavn = getValue(navn.getEtternavn());

        if (fornavn.isPresent() && mellomnavn.isPresent() && etternavn.isPresent()) {
            return String.format(SURNAME_GIVENNAME_MIDDLENAME, etternavn.get(), fornavn.get(), mellomnavn.get());
        } else if (fornavn.isPresent() && etternavn.isPresent()) {
            return String.format(SURNAME_GIVENNAME, etternavn.get(), fornavn.get());
        } else return etternavn.orElse("");
    }

    private static Optional<String> getValue(String value) {
        return StringUtils.isEmpty(value) ? Optional.empty() : Optional.of(value);
    }

    public static Customer toCustomer(PersonResource person) {
        Customer customer = new Customer();
        customer.setCustomerId(getCustomerId(person.getFodselsnummer().getIdentifikatorverdi()));
        customer.setDisplayName(getDisplayName(person.getNavn()));
        customer.setEmail(person.getKontaktinformasjon().getEpostadresse());
        customer.setMobile(person.getKontaktinformasjon().getMobiltelefonnummer());
        customer.setCity(person.getBostedsadresse().getPoststed());
        person.getBostedsadresse().getAdresselinje().stream().findFirst().ifPresent(customer::setPostalAddress);
        customer.setPostalCode(person.getBostedsadresse().getPostnummer());
        person.getSelfLinks().stream().map(Link::getHref).map(URI::create).findAny().ifPresent(customer::setPerson);
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