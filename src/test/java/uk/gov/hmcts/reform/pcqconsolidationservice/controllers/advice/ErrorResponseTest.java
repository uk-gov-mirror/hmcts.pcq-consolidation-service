package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.advice;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.advice.ErrorResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class ErrorResponseTest {

    @Test
    void testErrorResponse() {
        String expectMsg = "msg";

        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription("desc")
                .errorMessage(expectMsg)
                .timeStamp("time")
                .build();

        final String expectDesc = "desc";
        final String expectTs = "time";
        assertNotNull("Error Response is null", errorDetails);
        assertEquals("Error message is not expected", expectMsg, errorDetails.getErrorMessage());
        assertEquals("Timestamp is not correct", expectTs, errorDetails.getTimeStamp());
        assertEquals("Error description is not correct", expectDesc, errorDetails.getErrorDescription());
    }

    @Test
    void testNoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNotNull("ErrorResponse is null", errorResponse);
    }

}
