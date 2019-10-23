package no.fint.betaling.model.vocab;

import org.codehaus.jackson.annotate.JsonValue;

public enum BetalingStatus {
    SENT("sent"), STORED("stored"), ERROR("error");

    private final String betalingStatus;

    BetalingStatus(String betalingStatus) {
        this.betalingStatus = betalingStatus;
    }

    @JsonValue
    public String getBetalingStatus() {
        return betalingStatus;
    }
}
