package no.fint.betaling.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Data
public class User {
    private String name;
    private String employeeNumber;
    private Organisation organisation;
    private List<Organisation> organisationUnits;
    private long idleTime;
    private boolean isAdmin;
}