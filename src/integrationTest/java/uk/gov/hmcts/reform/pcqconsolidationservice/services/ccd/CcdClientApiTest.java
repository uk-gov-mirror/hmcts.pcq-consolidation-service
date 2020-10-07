package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;
import uk.gov.hmcts.reform.pcqconsolidationservice.util.SpringBootIntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@TestPropertySource(locations = "/application.properties")
public class CcdClientApiTest extends SpringBootIntegrationTest {

    private static final String CASE_SEARCH_URL = "/searchCases";
    private static final Long EXPECTED_CASE_ID = 1_988_575L;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";
    private static final String OPENID_TOKEN_ENDPOINT = "/o/token";
    private static final String OPENID_TOKEN_RESULT = String.format(
            "{\"access_token\":\"TOKEN\",\"token_type\":\"Bearer\","
                    + "\"scope\": \"openid profile roles\",\"expires_in\":28800}");

    @Autowired
    private CcdAuthenticatorFactory authenticatorFactory;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(WireMockConfiguration.options().port(4554));

    @Test
    public void testPcqWithoutCaseExecuteSuccess() {
        searchCasesMockSuccess();

        CcdClientApi ccdClientApi = new CcdClientApi(coreCaseDataApi, authenticatorFactory, serviceConfigProvider);

        List<Long> response = ccdClientApi.getCaseRefsByPcqId("1234", "pcqtestone", "applicant");

        Assert.assertEquals(1, response.size());
        Assert.assertEquals(EXPECTED_CASE_ID, response.get(0));

        WireMock.verify(1,postRequestedFor(urlEqualTo("/o/token")));
        WireMock.verify(1,postRequestedFor(urlEqualTo("/lease")));
        WireMock.verify(1,getRequestedFor(urlEqualTo("/details")));
    }

    public static String fileContentAsString(String file) {
        return new String(fileContentAsBytes(file), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public static byte[] fileContentAsBytes(String file) {
        try {
            return Resources.toByteArray(Resources.getResource(file));
        } catch (IOException e) {
            throw new RuntimeException("Could not load file" + file, e);
        }
    }

    private void searchCasesMockSuccess() {
        wireMockServer.stubFor(post(OPENID_TOKEN_ENDPOINT)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                        .withStatus(200)
                        .withBody(OPENID_TOKEN_RESULT)));

        wireMockServer.stubFor(post(urlPathMatching(CASE_SEARCH_URL))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                        .withStatus(200)
                        .withBody(fileContentAsString("ccd/searchCases/successful-response.json"))));

        wireMockServer.stubFor(get(urlPathMatching("/details"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                        .withStatus(200)
                        .withBody("{\"user_details\":\"user details\"}")));

        wireMockServer.stubFor(post(urlPathMatching("/lease"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                        .withStatus(200)
                        .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcm9iYXRlX2Zyb250ZW5kIiwiZXhwIjoxNTg4NTM2NjI2fQ."
                                + "UFtuWsLC2eGWa5WAuZ_Vqxe3PODACpe-1b0xz-wmhx_wsY3urJROLW1E5Fh6Goh_yrT67UdG3oTSHTxn"
                                + "ojdUkg")));
    }
}

