package uk.gov.hmcts.reform.pcqconsolidationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfiguration;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

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

    @Mock
    private CcdClientApi ccdClientApi;

    @Mock
    private ServiceConfiguration serviceConfiguration;

    @BeforeEach
    public void setUp() {
        testConsolidationComponent = new ConsolidationComponent(ccdClientApi, serviceConfiguration);
    }

    private static final String TEST_PCQ_ID_1 = "PCQ_ID1";
    private static final String TEST_PCQ_ID_2 = "PCQ_ID2";
    private static final String TEST_CASE_ID = "TEST-Case_Id";
    private static final String SUCCESS = "Success";

    @Test
    public void executeApiError() {
        ExternalApiException testException = new ExternalApiException(HttpStatus.BAD_GATEWAY, "Gateway Error");
        when(pcqBackendService.getPcqWithoutCase()).thenThrow(testException);

        assertThrows(ExternalApiException.class, () -> testConsolidationComponent.execute());

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiErrorAddCase() {
        ExternalApiException testException = new ExternalApiException(HttpStatus.BAD_GATEWAY,
                "Add Case Gateway Error");
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse(SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID)).thenThrow(testException);

        assertThrows(ExternalApiException.class, () -> testConsolidationComponent.execute());

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID);
    }

    @Test
    public void executeApiSuccess() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse(SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, SUCCESS, 200));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID);
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID);
    }

    @Test
    public void executeApiInvalidRequest() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse("Invalid Request", 400));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiInvalidRequestAddCase() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse(SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, "Invalid Request", 400));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID);
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID);
    }

    @Test
    public void executeApiInternalError() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse("Unknown error", 500));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
    }

    @Test
    public void executeApiInternalErrorAddCase() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse(SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, "Unknown error", 500));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID)).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, SUCCESS, 200));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID);
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID);
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

    @SuppressWarnings("unchecked")
    private ResponseEntity generateSubmitTestSuccessResponse(String pcqId, String message, int statusCode) {
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setResponseStatus(message);
        submitResponse.setResponseStatusCode(String.valueOf(statusCode));
        submitResponse.setPcqId(pcqId);

        return new ResponseEntity(submitResponse, HttpStatus.valueOf(statusCode));
    }


}