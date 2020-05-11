package uk.gov.hmcts.reform.pcqconsolidationservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigHelper;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfiguration;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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
    private ServiceConfiguration serviceConfiguration;

    @Mock
    private CcdClientApi ccdClientApi;

    private static final String TEST_PCQ_ID_1 = "PCQ_ID1";
    private static final String TEST_PCQ_ID_2 = "PCQ_ID2";
    private static final Long TEST_CASE_ID = 484_757_637_549L;
    private static final String SUCCESS = "Success";
    private static final String SERVICE_NAME = "Service1";
    private static final String CASE_TYPE_ID = "CaseTypeA";

    private static final ServiceConfigItem SERVICE_CONFIG =
            ServiceConfigHelper.serviceConfigItem(SERVICE_NAME, "jurisdiction1", Arrays.asList(CASE_TYPE_ID));


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
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString())).thenThrow(testException);
        when(serviceConfiguration.getServices()).thenReturn(Arrays.asList(SERVICE_CONFIG));
        when(ccdClientApi.getCaseRefsByPcqId(anyString(), anyString()))
                .thenReturn(Arrays.asList(TEST_CASE_ID));
        assertThrows(ExternalApiException.class, () -> testConsolidationComponent.execute());

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString());
        verify(serviceConfiguration, times(1)).getServices();
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_1, SERVICE_NAME);
    }

    @Test
    public void executeApiSuccess() {
        when(pcqBackendService.getPcqWithoutCase()).thenReturn(generateTestSuccessResponse(SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, SUCCESS, 200));
        when(serviceConfiguration.getServices()).thenReturn(Arrays.asList(SERVICE_CONFIG));
        when(ccdClientApi.getCaseRefsByPcqId(anyString(), anyString()))
                .thenReturn(Arrays.asList(TEST_CASE_ID));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString());
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString());
        verify(serviceConfiguration, times(2)).getServices();
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_1, SERVICE_NAME);
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_2, SERVICE_NAME);
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
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, SUCCESS, 200));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, "Invalid Request", 400));
        when(serviceConfiguration.getServices()).thenReturn(Arrays.asList(SERVICE_CONFIG));
        when(ccdClientApi.getCaseRefsByPcqId(anyString(), anyString()))
                .thenReturn(Arrays.asList(TEST_CASE_ID));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString());
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString());
        verify(serviceConfiguration, times(2)).getServices();
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_1, SERVICE_NAME);
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_2, SERVICE_NAME);
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
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_1, "Unknown error", 500));
        when(pcqBackendService.addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString())).thenReturn(
                generateSubmitTestSuccessResponse(TEST_PCQ_ID_2, SUCCESS, 200));
        when(serviceConfiguration.getServices()).thenReturn(Arrays.asList(SERVICE_CONFIG));
        when(ccdClientApi.getCaseRefsByPcqId(anyString(), anyString()))
                .thenReturn(Arrays.asList(TEST_CASE_ID));

        testConsolidationComponent.execute();

        verify(pcqBackendService, times(1)).getPcqWithoutCase();
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_1, TEST_CASE_ID.toString());
        verify(pcqBackendService, times(1)).addCaseForPcq(TEST_PCQ_ID_2, TEST_CASE_ID.toString());
        verify(serviceConfiguration, times(2)).getServices();
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_1, SERVICE_NAME);
        verify(ccdClientApi, times(1)).getCaseRefsByPcqId(TEST_PCQ_ID_2, SERVICE_NAME);
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