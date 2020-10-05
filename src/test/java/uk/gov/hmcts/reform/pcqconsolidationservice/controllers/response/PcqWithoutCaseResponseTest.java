package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

class PcqWithoutCaseResponseTest {

    private final String[] pcqIds = {"PCQ_ID1", "PCQ_ID2"};
    private static final String[] EMPTY_PCQID_ARRAY = {};
    private static final String STATUS = "Success";
    private static final String STATUS_CODE = "200";

    @Test
    void testPcqWithoutCaseResponse() {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        pcqWithoutCaseResponse.setPcqId(pcqIds);
        pcqWithoutCaseResponse.setResponseStatus(STATUS);
        pcqWithoutCaseResponse.setResponseStatusCode(STATUS_CODE);

        assertArrayEquals("PCQ Ids don't match", pcqIds, pcqWithoutCaseResponse.getPcqId());
        assertEquals("Response status doesn't match", STATUS, pcqWithoutCaseResponse.getResponseStatus());
        assertEquals("Response status code doesn't match", STATUS_CODE, pcqWithoutCaseResponse.getResponseStatusCode());
    }

    @Test
    void testPcqWithoutCaseResponseNullPcqs() {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        pcqWithoutCaseResponse.setPcqId(null);

        assertArrayEquals("PCQ Ids don't match", EMPTY_PCQID_ARRAY, pcqWithoutCaseResponse.getPcqId());
    }
}
