package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions;

public class InvalidFileException extends RuntimeException {
    private final String resource;
    private final String field;
    private final Object value;
    public InvalidFileException(String message, String resource, String field, Object value) {
        super("Invalid file for " + resource + " (" + field + "0" + value + ")");
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}
