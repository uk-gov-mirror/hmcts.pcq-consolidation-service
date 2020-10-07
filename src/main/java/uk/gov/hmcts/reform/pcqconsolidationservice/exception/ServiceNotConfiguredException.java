package uk.gov.hmcts.reform.pcqconsolidationservice.exception;

public class ServiceNotConfiguredException extends RuntimeException {

    private static final long serialVersionUID = -422128114862699005L;

    public ServiceNotConfiguredException(String message) {
        super(message);
    }
}