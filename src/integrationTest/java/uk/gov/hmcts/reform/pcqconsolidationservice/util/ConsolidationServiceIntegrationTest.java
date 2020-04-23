package uk.gov.hmcts.reform.pcqconsolidationservice.util;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertNotNull;

@TestPropertySource(properties = {"PCQ_BACKEND_URL:http://127.0.0.1:4554"})
public class ConsolidationServiceIntegrationTest extends SpringBootIntegrationTest {


    @Rule
    public WireMockRule pcqBackendService = new WireMockRule(WireMockConfiguration.options().port(4554));


    private void pcqWithoutCaseWireMockSuccess() {
        pcqBackendService.stubFor(get(urlPathMatching("/pcq/backend/consolidation/pcqWithoutCase"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"pcqId\": [\"c4402c47-c6dc-459e-884e-8f546781a5ab\","
                                + "\"67b4161f-dd1e-43ab-9511-d4161817e1d2\"], \"responseStatus\": \"Success\","
                                + "\"responseStatusCode\": \"200\"}")));
    }


    @Test
    public void testPcqWithoutCaseExecuteSuccess() {
        pcqWithoutCaseWireMockSuccess();

        ResponseEntity responseEntity = pcqBackendServiceImpl.getPcqWithoutCase();
        assertNotNull("", responseEntity);
    }

}
