package uk.gov.hmcts.reform.pcqconsolidationservice.utils;

import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqRecordWithoutCaseResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.CloseResource",
        "PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
@Slf4j
public class JsonFeignResponseUtilTest {

    private static final String ENCODING_STR = "content-encoding";

    @Test
    @SuppressWarnings("unchecked")
    public void testDecode() {
        Map<String, Collection<String>> header = new ConcurrentHashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put(ENCODING_STR, list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(
                "{"
                        + "    \"pcqRecord\": ["
                        + "        {"
                        + "            \"pcqAnswers\": null,"
                        + "            \"pcqId\": \"d1bc52bc-b673-46d3-a0d8-052ef678772e\","
                        + "            \"ccdCaseId\": null,"
                        + "            \"partyId\": null,"
                        + "            \"channel\": null,"
                        + "            \"completedDate\": null,"
                        + "            \"serviceId\": \"PCQTEST\","
                        + "            \"actor\": \"DEFENDANT\","
                        + "            \"versionNo\": null"
                        + "        },"
                        + "        {"
                        + "            \"pcqAnswers\": null,"
                        + "            \"pcqId\": \"27f29282-6ff5-4a06-9277-fea8058a07a9\","
                        + "            \"ccdCaseId\": null,"
                        + "            \"partyId\": null,"
                        + "            \"channel\": null,"
                        + "            \"completedDate\": null,"
                        + "            \"serviceId\": \"PCQTEST\","
                        + "            \"actor\": \"DEFENDANT\","
                        + "            \"versionNo\": null"
                        + "        }"
                        + "    ],"
                        + "    \"responseStatus\": \"Success\","
                        + "    \"responseStatusCode\": \"200\""
                        + "}", UTF_8).request(mock(Request.class)).build();
        Optional<Object> pcqRecordWithoutCaseResponseOptional = Optional.empty();
        try {
            pcqRecordWithoutCaseResponseOptional = JsonFeignResponseUtil.decode(response,
                    PcqRecordWithoutCaseResponse.class);
        } catch (IOException e) {
            log.error("IOException occurred {} ", e.getMessage());
            fail("Not expected to get IO Exception here");
        } finally {
            response.close();
        }


        assertThat(pcqRecordWithoutCaseResponseOptional).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecode_fails_with_ioException() {
        Map<String, Collection<String>> header = new ConcurrentHashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put(ENCODING_STR, list);

        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(
                mock(Request.class)).build();

        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
            bodyMock.close();
        } catch (IOException e) {
            log.error("Error during execution {}", e.getMessage());
        }

        Optional<Object> createUserProfileResponseOptional = Optional.empty();
        try {
            createUserProfileResponseOptional = JsonFeignResponseUtil.decode(response,
                    PcqRecordWithoutCaseResponse.class);
        } catch (IOException e) {
            log.error("Error during execution {}", e.getMessage());
        } finally {
            response.close();
        }

        assertThat(createUserProfileResponseOptional).isEmpty();


    }

    @Test
    public void test_convertHeaders() {
        Map<String, Collection<String>> header = new ConcurrentHashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("gzip", "request-context", "x-powered-by",
                "content-length"));
        header.put(ENCODING_STR, list);

        MultiValueMap<String, String> responseHeader = JsonFeignResponseUtil.convertHeaders(header);
        assertThat(responseHeader).isNotEmpty();

        Collection<String> emptylist = new ArrayList<>();
        header.put(ENCODING_STR, emptylist);
        MultiValueMap<String, String> responseHeader1 = JsonFeignResponseUtil.convertHeaders(header);

        assertThat(responseHeader1.get(ENCODING_STR)).isEmpty();
    }

    @Test
    public void test_toResponseEntity_with_payload_not_empty() {
        Map<String, Collection<String>> header = new ConcurrentHashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        header.put(ENCODING_STR, list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(
                "{"
                        + "    \"pcqRecord\": ["
                        + "        {"
                        + "            \"pcqAnswers\": null,"
                        + "            \"pcqId\": \"d1bc52bc-b673-46d3-a0d8-052ef678772e\","
                        + "            \"ccdCaseId\": null,"
                        + "            \"partyId\": null,"
                        + "            \"channel\": null,"
                        + "            \"completedDate\": null,"
                        + "            \"serviceId\": \"PCQTEST\","
                        + "            \"actor\": \"DEFENDANT\","
                        + "            \"versionNo\": null"
                        + "        },"
                        + "        {"
                        + "            \"pcqAnswers\": null,"
                        + "            \"pcqId\": \"27f29282-6ff5-4a06-9277-fea8058a07a9\","
                        + "            \"ccdCaseId\": null,"
                        + "            \"partyId\": null,"
                        + "            \"channel\": null,"
                        + "            \"completedDate\": null,"
                        + "            \"serviceId\": \"PCQTEST\","
                        + "            \"actor\": \"DEFENDANT\","
                        + "            \"versionNo\": null"
                        + "        }"
                        + "    ],"
                        + "    \"responseStatus\": \"Success\","
                        + "    \"responseStatusCode\": \"200\""
                        + "}", UTF_8).request(mock(Request.class)).build();
        ResponseEntity entity = null;
        try {
            entity = JsonFeignResponseUtil.toResponseEntity(response, PcqRecordWithoutCaseResponse.class);
        } catch (IOException e) {
            log.error("IOException occurred {}", e.getMessage());
            fail("Not Expected IO Exception here.");
        } finally {
            response.close();
        }

        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        PcqAnswerResponse[] pcqAnswerResponses = ((PcqRecordWithoutCaseResponse) entity.getBody()).getPcqRecord();
        for (PcqAnswerResponse pcqAnswerResponse : pcqAnswerResponses) {
            assertTrue("", pcqAnswerResponse.getPcqId().equals("d1bc52bc-b673-46d3-a0d8-052ef678772e")
                    || pcqAnswerResponse.getPcqId().equals("27f29282-6ff5-4a06-9277-fea8058a07a9"));
        }
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseUtil> constructor = JsonFeignResponseUtil.class.getDeclaredConstructor();
        assertFalse("Constructor is not accessible", constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
