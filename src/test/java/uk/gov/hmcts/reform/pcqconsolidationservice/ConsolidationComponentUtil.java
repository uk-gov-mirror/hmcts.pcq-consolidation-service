package uk.gov.hmcts.reform.pcqconsolidationservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;

public final class ConsolidationComponentUtil {

    private ConsolidationComponentUtil() {
        //not called
    }

    @SuppressWarnings("unchecked")
    public static ResponseEntity generateSubmitTestSuccessResponse(String pcqId, String message, int statusCode) {
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setResponseStatus(message);
        submitResponse.setResponseStatusCode(String.valueOf(statusCode));
        submitResponse.setPcqId(pcqId);

        return new ResponseEntity(submitResponse, HttpStatus.valueOf(statusCode));
    }

    public static PcqAnswerResponse generateTestAnswer(String pcqId, String serviceId, String actor) {
        PcqAnswerResponse answerResponse = new PcqAnswerResponse();
        answerResponse.setPcqId(pcqId);
        answerResponse.setServiceId(serviceId);
        answerResponse.setActor(actor);

        return answerResponse;
    }

}
