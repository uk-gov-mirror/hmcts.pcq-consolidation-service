package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PcqAnswerResponseTest {

    private static final String PCQ_ID = "TEST_PCQ";
    private static final String CASE_ID = "CASE_ID";
    private static final String PARTY_ID = "PARTY_ID";
    private static final Integer CHANNEL = 1;
    private static final String SERVICE_ID = "Service_1";
    private static final String ACTOR = "Actor_1";
    private static final Integer VERSION_NO = 1;

    @Test
    public void testPcqAnswerResponse() {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();
        answerResponse.setActor(ACTOR);
        answerResponse.setServiceId(SERVICE_ID);
        answerResponse.setPcqId(PCQ_ID);
        answerResponse.setCaseId(CASE_ID);
        answerResponse.setChannel(CHANNEL);
        answerResponse.setCompletedDate(null);
        answerResponse.setPartyId(PARTY_ID);
        answerResponse.setVersionNo(VERSION_NO);
        answerResponse.setPcqAnswers(null);

        assertEquals("Actor is not matching", ACTOR, answerResponse.getActor());
        assertEquals("Service is not matching", SERVICE_ID, answerResponse.getServiceId());
        assertEquals("PCQ ID is not matching", PCQ_ID, answerResponse.getPcqId());
        assertEquals("Case ID is not matching", CASE_ID, answerResponse.getCaseId());
        assertEquals("Channel is not matching", CHANNEL, answerResponse.getChannel());
        assertNull("Completed Date is not matching", answerResponse.getCompletedDate());
        assertEquals("Party is not matching", PARTY_ID, answerResponse.getPartyId());
        assertEquals("Version is not matching", VERSION_NO, answerResponse.getVersionNo());
        assertNull("Pcq Answer is not matching", answerResponse.getPcqAnswers());
    }
}
