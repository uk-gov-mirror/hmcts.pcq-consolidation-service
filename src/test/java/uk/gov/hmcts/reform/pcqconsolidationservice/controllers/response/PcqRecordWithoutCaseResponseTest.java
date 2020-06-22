package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqRecordWithoutCaseResponse;

import static org.junit.Assert.assertEquals;

public class PcqRecordWithoutCaseResponseTest {
    private static final String STATUS = "Success";
    private static final String STATUS_CODE = "200";

    @Test
    public void testPcqRecordWithoutCaseResponse() {
        PcqRecordWithoutCaseResponse pcqRecordWithoutCaseResponse = new PcqRecordWithoutCaseResponse();
        PcqAnswerResponse answerResponse1 = generateTestAnswer("PCQ_ID1", "SERVICE_JD1", "ACTOR_1");
        PcqAnswerResponse answerResponse2 = generateTestAnswer("PCQ_ID2", "SERVICE_JD2", "ACTOR_2");

        PcqAnswerResponse[] answerResponses = {answerResponse1, answerResponse2};
        pcqRecordWithoutCaseResponse.setPcqRecord(answerResponses);
        pcqRecordWithoutCaseResponse.setResponseStatus(STATUS);
        pcqRecordWithoutCaseResponse.setResponseStatusCode(STATUS_CODE);

        assertPcqRecordsEqual(answerResponses, pcqRecordWithoutCaseResponse.getPcqRecord());
        assertEquals("Response status doesn't match", STATUS, pcqRecordWithoutCaseResponse.getResponseStatus());
        assertEquals("Response status code doesn't match", STATUS_CODE,
                pcqRecordWithoutCaseResponse.getResponseStatusCode());
    }

    @SuppressWarnings("PMD.UseVarargs")
    private void assertPcqRecordsEqual(PcqAnswerResponse[] originalAnswers, PcqAnswerResponse[] responseAnswers) {
        assertEquals("Pcq Answers Array Length don't match", originalAnswers.length, responseAnswers.length);
        for (int i = 0; i < originalAnswers.length; i++) {
            assertEquals("PCQ Ids don't match", originalAnswers[i].getPcqId(), responseAnswers[i].getPcqId());
            assertEquals("Service Ids don't match", originalAnswers[i].getServiceId(),
                    responseAnswers[i].getServiceId());
            assertEquals("Actors don't match", originalAnswers[i].getActor(), responseAnswers[i].getActor());
        }
    }

    private PcqAnswerResponse generateTestAnswer(String pcqId, String serviceId, String actor) {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();
        answerResponse.setPcqId(pcqId);
        answerResponse.setServiceId(serviceId);
        answerResponse.setActor(actor);

        return answerResponse;
    }
}
