package uk.gov.hmcts.reform.pcqconsolidationservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalApiException extends RuntimeException {

    public static final long serialVersionUID = 43287452;

    private final HttpStatus httpStatus;

    private final String errorMessage;

    public ExternalApiException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}