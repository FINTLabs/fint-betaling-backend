package no.fint.betaling.factory;

import no.fint.betaling.model.Customer;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.util.StringUtils;

import java.util.Optional;

public enum CustomerFactory {
    ;
    private static final String LASTNAME_FIRSTNAME_MIDDLENAME = "%s, %s %s";
    private static final String LASTNAME_FIRSTNAME = "%s, %s";

    public static String getDisplayName(Personnavn name) {
        if (name == null) return null;

        Optional<String> firstName = getValue(name.getFornavn());
        Optional<String> middleName = getValue(name.getMellomnavn());
        Optional<String> lastName = getValue(name.getEtternavn());

        if (firstName.isPresent() && middleName.isPresent() && lastName.isPresent()) {
            return String.format(LASTNAME_FIRSTNAME_MIDDLENAME, lastName.get(), firstName.get(), middleName.get());
        } else if (firstName.isPresent() && lastName.isPresent()) {
            return String.format(LASTNAME_FIRSTNAME, lastName.get(), firstName.get());
        } else return lastName.orElse("");
    }

    private static Optional<String> getValue(String value) {
        return StringUtils.isEmpty(value) ? Optional.empty() : Optional.of(value);
    }

    public static Customer toCustomer(PersonResource person) {
        Customer customer = new Customer();
        customer.setId(getCustomerId(person.getFodselsnummer().getIdentifikatorverdi()));
        customer.setName(getDisplayName(person.getNavn()));
        customer.setEmail(person.getKontaktinformasjon().getEpostadresse());
        customer.setMobile(person.getKontaktinformasjon().getMobiltelefonnummer());
        customer.setCity(person.getPostadresse().getPoststed());
        person.getPostadresse().getAdresselinje().stream().findFirst().ifPresent(customer::setPostalAddress);
        customer.setPostalCode(person.getPostadresse().getPostnummer());
        person.getSelfLinks().stream().map(Link::getHref).findAny().ifPresent(customer::setPerson);
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