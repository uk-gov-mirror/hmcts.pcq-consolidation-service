package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PcqWithoutCaseResponseTest {

    private final String[] pcqIds = {"PCQ_ID1", "PCQ_ID2"};
    private final String status = "Success";
    private final String statusCode = "200";

    @Test
    public void testPcqWithoutCaseResponse() {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        pcqWithoutCaseResponse.setPcqId(pcqIds);
        pcqWithoutCaseResponse.setResponseStatus(status);
        pcqWithoutCaseResponse.setResponseStatusCode(statusCode);

        assertArrayEquals("PCQ Ids don't match", pcqIds, pcqWithoutCaseResponse.getPcqId());
        assertEquals("Response status doesn't match", status, pcqWithoutCaseResponse.getResponseStatus());
        assertEquals("Response status code doesn't match", statusCode, pcqWithoutCaseResponse.getResponseStatusCode());
    }
}
