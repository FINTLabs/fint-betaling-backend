package no.fint.betaling.model;

import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KundeFactory {

    public static String getPersonnavnAsString(Personnavn navn) {
        if (navn == null) return null;
        String result = "";
        if (!StringUtils.isEmpty(navn.getEtternavn()))
            result += navn.getEtternavn();
        if (!StringUtils.isEmpty(navn.getFornavn())) {
            if (!result.isEmpty())
                result += ", ";
            result += navn.getFornavn();
        }
        if (!StringUtils.isEmpty(navn.getMellomnavn()))
            result += " " + navn.getMellomnavn();
        return result;
    }

    public Kunde getKunde(PersonResource person) {
        Kunde customer = new Kunde();
        customer.setKundeid(getCustomerId(person.getFodselsnummer().getIdentifikatorverdi()));
        customer.setNavn(getPersonnavnAsString(person.getNavn()));
        person.getSelfLinks().stream().findAny().ifPresent(customer::setPerson);
        person.getElev().stream().findAny().ifPresent(customer::setElev);
        return customer;
    }

    public static String getCustomerId(String nin) {
        return Long.toString((long) (Long.parseLong(nin) / 100), 36);
    }

}

