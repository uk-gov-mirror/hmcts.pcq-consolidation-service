package uk.gov.hmcts.reform.pcqconsolidationservice.util;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertNotNull;

@TestPropertySource(properties = {"PCQ_BACKEND_URL:http://127.0.0.1:4554"})
@SuppressWarnings("PMD.TooManyMethods")
public class ConsolidationServiceIntegrationTest extends SpringBootIntegrationTest {

    private static final String CASE_ID_TEST = "TEST_CASE_ID";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_TYPE = "application/json";

    @Rule
    public WireMockRule pcqBackendService = new WireMockRule(WireMockConfiguration.options().port(4554));

    @Test
    public void testPcqWithoutCaseExecuteSuccess() {
        pcqWithoutCaseWireMockSuccess();

        ResponseEntity responseEntity = pcqBackendServiceImpl.getPcqWithoutCase();
        assertNotNull("", responseEntity);
    }

    @Test
    public void testPcqWithoutCaseExecuteInvalidError() {
        pcqWithoutCaseWireMockFailure();

        ResponseEntity responseEntity = pcqBackendServiceImpl.getPcqWithoutCase();
        assertNotNull("", responseEntity);
    }

    @Test
    public void testPcqWithoutCaseExecuteInternalError() {
        pcqWithoutCaseWireMockInternalError();

        ResponseEntity responseEntity = pcqBackendServiceImpl.getPcqWithoutCase();
        assertNotNull("", responseEntity);
    }

    @Test
    public void testAddCaseForPcqExecuteSuccess() {
        pcqAddCaseWireMockSuccess();

        ResponseEntity responseEntity = pcqBackendServiceImpl.addCaseForPcq("TEST_PCQ_ID", CASE_ID_TEST);
        assertNotNull("", responseEntity);
    }

    @Test
    public void testAddCaseForPcqExecuteInvalidRequest() {
        pcqAddCaseWireMockInvalidRequest();

        ResponseEntity responseEntity = pcqBackendServiceImpl.addCaseForPcq("TEST_PCQ_ID", CASE_ID_TEST);
        assertNotNull("", responseEntity);
    }

    @Test
    public void testAddCaseForPcqExecuteInternalError() {
        pcqAddCaseWireMockInternalError();

        ResponseEntity responseEntity = pcqBackendServiceImpl.addCaseForPcq("TEST_PCQ_ID", CASE_ID_TEST);
        assertNotNull("", responseEntity);
    }


    private void pcqWithoutCaseWireMockSuccess() {
        pcqBackendService.stubFor(get(urlPathMatching("/pcq/backend/consolidation/pcqWithoutCase"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(200)
                        .withBody("{\"pcqId\": [\"c4402c47-c6dc-459e-884e-8f546781a5ab\","
                                + "\"67b4161f-dd1e-43ab-9511-d4161817e1d2\"], \"responseStatus\": \"Success\","
                                + "\"responseStatusCode\": \"200\"}")));
    }

    private void pcqWithoutCaseWireMockFailure() {
        pcqBackendService.stubFor(get(urlPathMatching("/pcq/backend/consolidation/pcqWithoutCase"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(400)
                        .withBody("{\"responseStatus\": \"Invalid Request\","
                                + "\"responseStatusCode\": \"400\"}")));
    }

    private void pcqWithoutCaseWireMockInternalError() {
        pcqBackendService.stubFor(get(urlPathMatching("/pcq/backend/consolidation/pcqWithoutCase"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(500)
                        .withBody("{\"responseStatus\": \"Unknown error occurred\","
                                + "\"responseStatusCode\": \"500\"}")));
    }

    private void pcqAddCaseWireMockSuccess() {
        pcqBackendService.stubFor(put(urlPathMatching("/pcq/backend/consolidation/addCaseForPCQ/TEST_PCQ_ID"))
                .withQueryParam("caseId", equalTo(CASE_ID_TEST))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(200)
                        .withBody("{\"pcqId\": \"TEST_PCQ_ID\","
                                + "\"responseStatus\": \"Success\","
                                + "\"responseStatusCode\": \"200\"}")));
    }

    private void pcqAddCaseWireMockInvalidRequest() {
        pcqBackendService.stubFor(put(urlPathMatching("/pcq/backend/consolidation/addCaseForPCQ/TEST_PCQ_ID"))
                .withQueryParam("caseId", equalTo(CASE_ID_TEST))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(400)
                        .withBody("{\"pcqId\": \"TEST_PCQ_ID\","
                                + "\"responseStatus\": \"Invalid Request\","
                                + "\"responseStatusCode\": \"400\"}")));
    }

    private void pcqAddCaseWireMockInternalError() {
        pcqBackendService.stubFor(put(urlPathMatching("/pcq/backend/consolidation/addCaseForPCQ/TEST_PCQ_ID"))
                .withQueryParam("caseId", equalTo(CASE_ID_TEST))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MEDIA_TYPE)
                        .withStatus(500)
                        .withBody("{\"pcqId\": \"TEST_PCQ_ID\","
                                + "\"responseStatus\": \"Unknown error occurred\","
                                + "\"responseStatusCode\": \"500\"}")));
    }


}
