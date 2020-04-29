package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;

import static org.junit.Assert.assertEquals;

public class SubmitResponseTest {

    private static final String PCQ_ID = "PCQ_ID1";
    private static final String STATUS = "Success";
    private static final String STATUS_CODE = "200";

    @Test
    public void testSubmitResponse() {
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setPcqId(PCQ_ID);
        submitResponse.setResponseStatus(STATUS);
        submitResponse.setResponseStatusCode(STATUS_CODE);

        assertEquals("PCQ Ids don't match", PCQ_ID, submitResponse.getPcqId());
        assertEquals("Response status doesn't match", STATUS, submitResponse.getResponseStatus());
        assertEquals("Response status code doesn't match", STATUS_CODE, submitResponse.getResponseStatusCode());
    }
}
