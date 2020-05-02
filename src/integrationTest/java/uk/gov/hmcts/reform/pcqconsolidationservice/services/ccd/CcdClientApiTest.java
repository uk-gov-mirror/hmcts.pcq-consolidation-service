package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@TestPropertySource(locations = "/application.properties")
public class CcdClientApiTest extends SpringBootIntegrationTest {

    @Autowired
    private CcdAuthenticatorFactory authenticatorFactory;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Rule
    public WireMockRule coreCaseDataRule = new WireMockRule(WireMockConfiguration.options().port(4554));

    private void searchCasesMockSuccess() {
        coreCaseDataRule.stubFor(get(urlPathMatching("/searchCases"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(fileContentAsString("ccd/searchCases/successful-response.json"))));

        coreCaseDataRule.stubFor(post(urlPathMatching("/oauth2/authorize"))
                .withHeader("Authorization", containing("Basic cGNxLWV4dHJhY3RvcitjY2RAZ21haWwuY29tOlBhNTV3b3JkMTE="))
                .withHeader("Accept", containing("*/*"))

                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("6a49f75e-0ee5-4a76-a9e5-1d581b35b607")));
    }

    @Test
    public void testPcqWithoutCaseExecuteSuccess() {

        searchCasesMockSuccess();

        CcdClientApi ccdClientApi = new CcdClientApi(coreCaseDataApi, authenticatorFactory, serviceConfigProvider);

        List<Long> response = ccdClientApi.getCaseRefsByPcqId("1234", "pcqtestone");

        //ResponseEntity responseEntity = pcqBackendServiceImpl.getPcqWithoutCase();
        //assertNotNull("", responseEntity);

        Assert.assertNotNull(response);
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
}

