package uk.gov.hmcts.reform.pcqconsolidationservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsolidationComponentTest {

    @InjectMocks
    private ConsolidationComponent testConsolidationComponent;

    @Mock
    private PcqBackendService pcqBackendService;

    @Test
    public void executeApiError() {
        ExternalApiException testException = new ExternalApiException(HttpStatus.BAD_GATEWAY, "Gateway Error");
        when(pcqBackendService.getPcqWithoutCase()).thenThrow(testException);

        assertThrows(ExternalApiException.class, () -> testConsolidationComponent.execute());

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiSuccess() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse("Success", 200));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiInvalidRequest() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse("Invalid Request", 400));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiInternalError() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse("Unknown error", 500));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity generateTestSuccessResponse(String message, int statusCode) {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        pcqWithoutCaseResponse.setResponseStatus(message);
        pcqWithoutCaseResponse.setResponseStatusCode(String.valueOf(statusCode));

        String[] pcqIds = {"PCQ_ID1", "PCQ_ID2"};
        pcqWithoutCaseResponse.setPcqId(pcqIds);

        return new ResponseEntity(pcqWithoutCaseResponse, HttpStatus.valueOf(statusCode));
    }


}
