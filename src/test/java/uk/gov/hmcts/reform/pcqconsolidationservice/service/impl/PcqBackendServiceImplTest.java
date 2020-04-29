package uk.gov.hmcts.reform.pcqconsolidationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign.PcqBackendFeignClient;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyMethods")
public class PcqBackendServiceImplTest {

    private final PcqBackendFeignClient mockPcqBackendFeignClient = mock(PcqBackendFeignClient.class);

    private final PcqBackendServiceImpl pcqBackendService = new PcqBackendServiceImpl(mockPcqBackendFeignClient);

    private static final String HEADER_VALUE = null;
    private static final String RESPONSE_INCORRECT = "Not the correct response";
    private static final int STATUS_OK = 200;
    private static final String TEST_PCQ_ID = "UNIT_TEST_PCQ_1";
    private static final String TEST_CASE_ID = "UNIT_TEST_CASE_1";
    private static final String EXPECTED_MSG_1 = "PcqIds don't match";
    private static final String EXPECTED_MSG_2 = "Status code not correct";
    private static final String EXPECTED_MSG_3 = "Status not correct";

    @Test
    public void testSuccess200Response() throws JsonProcessingException {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = generateTestResponse("Success", 200);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(pcqWithoutCaseResponse);

        when(mockPcqBackendFeignClient.getPcqWithoutCase(HEADER_VALUE)).thenReturn(Response.builder().request(mock(
                Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity responseEntity = pcqBackendService.getPcqWithoutCase();

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof PcqWithoutCaseResponse);
        PcqWithoutCaseResponse responseBody = (PcqWithoutCaseResponse) responseEntity.getBody();
        assertArrayEquals(EXPECTED_MSG_1, pcqWithoutCaseResponse.getPcqId(), responseBody.getPcqId());
        assertEquals(EXPECTED_MSG_2, pcqWithoutCaseResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, pcqWithoutCaseResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).getPcqWithoutCase(HEADER_VALUE);

    }

    @Test
    public void testSuccess200Response2() throws JsonProcessingException {
        SubmitResponse submitResponse = generateSubmitTestResponse("Success", 200);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(submitResponse);

        when(mockPcqBackendFeignClient.addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID)).thenReturn(
                Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(200).build());

        ResponseEntity responseEntity = pcqBackendService.addCaseForPcq(TEST_PCQ_ID, TEST_CASE_ID);

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof SubmitResponse);
        SubmitResponse responseBody = (SubmitResponse) responseEntity.getBody();
        assertEquals(EXPECTED_MSG_1, TEST_PCQ_ID, responseBody.getPcqId());
        assertEquals(EXPECTED_MSG_2, submitResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, submitResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID);

    }

    @Test
    public void testInvalidRequestErrorResponse() throws JsonProcessingException {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = generateTestResponse("Invalid Request", 400);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(pcqWithoutCaseResponse);

        when(mockPcqBackendFeignClient.getPcqWithoutCase(HEADER_VALUE)).thenReturn(Response.builder().request(mock(
                Request.class)).body(body, Charset.defaultCharset()).status(400).build());

        ResponseEntity responseEntity = pcqBackendService.getPcqWithoutCase();

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof PcqWithoutCaseResponse);
        PcqWithoutCaseResponse responseBody = (PcqWithoutCaseResponse) responseEntity.getBody();
        assertEquals("PcqIds size don't match", 0, responseBody.getPcqId().length);
        assertEquals(EXPECTED_MSG_2, pcqWithoutCaseResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, pcqWithoutCaseResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).getPcqWithoutCase(HEADER_VALUE);

    }

    @Test
    public void testInvalidRequestErrorResponse2() throws JsonProcessingException {
        SubmitResponse submitResponse = generateSubmitTestResponse("Invalid Request", 400);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(submitResponse);

        when(mockPcqBackendFeignClient.addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID)).thenReturn(
                Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(400).build());

        ResponseEntity responseEntity = pcqBackendService.addCaseForPcq(TEST_PCQ_ID, TEST_CASE_ID);

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof SubmitResponse);
        SubmitResponse responseBody = (SubmitResponse) responseEntity.getBody();
        assertNull("PCQ_ID not expected here", responseBody.getPcqId());
        assertEquals(EXPECTED_MSG_2, submitResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, submitResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID);

    }

    @Test
    public void testUnknownErrorResponse() throws JsonProcessingException {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = generateTestResponse("Unknown error occurred", 500);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(pcqWithoutCaseResponse);

        when(mockPcqBackendFeignClient.getPcqWithoutCase(HEADER_VALUE)).thenReturn(Response.builder().request(mock(
                Request.class)).body(body, Charset.defaultCharset()).status(500).build());

        ResponseEntity responseEntity = pcqBackendService.getPcqWithoutCase();

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof PcqWithoutCaseResponse);
        PcqWithoutCaseResponse responseBody = (PcqWithoutCaseResponse) responseEntity.getBody();
        assertEquals("PcqIds size don't match", 0, responseBody.getPcqId().length);
        assertEquals(EXPECTED_MSG_2, pcqWithoutCaseResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, pcqWithoutCaseResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).getPcqWithoutCase(HEADER_VALUE);

    }

    @Test
    public void testUnknownErrorResponse2() throws JsonProcessingException {
        SubmitResponse submitResponse = generateSubmitTestResponse("Unknown error occurred", 500);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(submitResponse);

        when(mockPcqBackendFeignClient.addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID)).thenReturn(
                Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500)
                        .build());

        ResponseEntity responseEntity = pcqBackendService.addCaseForPcq(TEST_PCQ_ID, TEST_CASE_ID);

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof SubmitResponse);
        SubmitResponse responseBody = (SubmitResponse) responseEntity.getBody();
        assertNull("PCQ_ID not expected here", responseBody.getPcqId());
        assertEquals(EXPECTED_MSG_2, submitResponse.getResponseStatusCode(),
                responseBody.getResponseStatusCode());
        assertEquals(EXPECTED_MSG_3, submitResponse.getResponseStatus(),
                responseBody.getResponseStatus());


        verify(mockPcqBackendFeignClient, times(1)).addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID);

    }

    @Test
    public void testOtherErrorResponse() throws JsonProcessingException {
        ErrorResponse errorResponse = generateErrorResponse();
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(errorResponse);

        when(mockPcqBackendFeignClient.getPcqWithoutCase(HEADER_VALUE)).thenReturn(Response.builder().request(mock(
                Request.class)).body(body, Charset.defaultCharset()).status(503).build());

        ResponseEntity responseEntity = pcqBackendService.getPcqWithoutCase();

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof PcqWithoutCaseResponse);
        PcqWithoutCaseResponse responseBody = (PcqWithoutCaseResponse) responseEntity.getBody();
        assertNull("", responseBody.getPcqId());
        assertNull("", responseBody.getResponseStatus());
        assertNull("", responseBody.getResponseStatusCode());

    }

    @Test
    public void testOtherErrorResponse2() throws JsonProcessingException {
        ErrorResponse errorResponse = generateErrorResponse();
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(errorResponse);

        when(mockPcqBackendFeignClient.addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID)).thenReturn(Response
                .builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(503).build());

        ResponseEntity responseEntity = pcqBackendService.addCaseForPcq(TEST_PCQ_ID, TEST_CASE_ID);

        assertTrue(RESPONSE_INCORRECT, responseEntity.getBody() instanceof SubmitResponse);
        SubmitResponse responseBody = (SubmitResponse) responseEntity.getBody();
        assertNull("", responseBody.getPcqId());
        assertNull("", responseBody.getResponseStatus());
        assertNull("", responseBody.getResponseStatusCode());

    }

    @Test
    public void executeFeignApiError() {
        ExternalApiException testException = new ExternalApiException(HttpStatus.BAD_GATEWAY, "Gateway Error");
        when(mockPcqBackendFeignClient.getPcqWithoutCase(HEADER_VALUE)).thenThrow(testException);

        assertThrows(ExternalApiException.class, () -> pcqBackendService.getPcqWithoutCase());

        verify(mockPcqBackendFeignClient, times(1)).getPcqWithoutCase(HEADER_VALUE);
    }

    @Test
    public void executeFeignApiError2() {
        ExternalApiException testException = new ExternalApiException(HttpStatus.BAD_GATEWAY, "Gateway Error");
        when(mockPcqBackendFeignClient.addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID,
                TEST_CASE_ID)).thenThrow(testException);

        assertThrows(ExternalApiException.class, () -> pcqBackendService.addCaseForPcq(TEST_PCQ_ID, TEST_CASE_ID));

        verify(mockPcqBackendFeignClient, times(1)).addCaseForPcq(HEADER_VALUE, TEST_PCQ_ID, TEST_CASE_ID);
    }


    private PcqWithoutCaseResponse generateTestResponse(String message, int statusCode) {
        PcqWithoutCaseResponse pcqWithoutCaseResponse = new PcqWithoutCaseResponse();
        pcqWithoutCaseResponse.setResponseStatus(message);
        pcqWithoutCaseResponse.setResponseStatusCode(String.valueOf(statusCode));

        if (statusCode == STATUS_OK) {
            String[] pcqIds = {"PCQ_ID1", "PCQ_ID2"};
            pcqWithoutCaseResponse.setPcqId(pcqIds);
        }

        return pcqWithoutCaseResponse;
    }

    private SubmitResponse generateSubmitTestResponse(String message, int statusCode) {
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setResponseStatus(message);
        submitResponse.setResponseStatusCode(String.valueOf(statusCode));

        if (statusCode == STATUS_OK) {
            String pcqIds = "UNIT_TEST_PCQ_1";
            submitResponse.setPcqId(pcqIds);
        }

        return submitResponse;
    }

    private ErrorResponse generateErrorResponse() {

        return new ErrorResponse("Bad Gateway",
                "Server did not respond", LocalDateTime.now().toString());
    }
}
