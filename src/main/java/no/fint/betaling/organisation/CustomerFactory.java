package no.fint.betaling.organisation;

import no.fint.betaling.model.Customer;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.felles.PersonResource;
import org.apache.commons.lang3.StringUtils;

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
        customer.setId(getCustomerId(person));
        customer.setName(getDisplayName(person.getNavn()));
        return customer;
    }

    public static String getCustomerId(PersonResource personResource) {
        String nin = Optional.ofNullable(personResource).map(PersonResource::getFodselsnummer).map(Identifikator::getIdentifikatorverdi).orElse("");
        try {
            return Long.toString((Long.parseLong(nin) / 100), 36);
        } catch (NumberFormatException ex) {
            return nin;
        }
    }
}