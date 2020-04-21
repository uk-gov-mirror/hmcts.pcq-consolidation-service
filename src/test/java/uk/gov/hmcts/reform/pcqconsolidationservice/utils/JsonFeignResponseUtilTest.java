package uk.gov.hmcts.reform.pcqconsolidationservice.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.Request;
import feign.Response;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;

public class JsonFeignResponseUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testDecode() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(
                "{\"pcqId\": [\"c4402c47-c6dc-459e-884e-8f546781a5ab\"," +
                        "\"67b4161f-dd1e-43ab-9511-d4161817e1d2\"], \"responseStatus\": \"Success\"," +
                        "\"responseStatusCode\": \"200\"}", UTF_8).request(mock(Request.class)).build();
        Optional<Object> pcqWithoutCaseResponseOptional = JsonFeignResponseUtil.decode(response,
                PcqWithoutCaseResponse.class);

        assertThat(pcqWithoutCaseResponseOptional).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecode_fails_with_ioException() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(
                mock(Request.class)).build();

        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<Object> createUserProfileResponseOptional = JsonFeignResponseUtil.decode(response, PcqWithoutCaseResponse.class);
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_convertHeaders() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("gzip", "request-context", "x-powered-by",
                "content-length"));
        header.put("content-encoding", list);

        MultiValueMap<String, String> responseHeader = JsonFeignResponseUtil.convertHeaders(header);
        assertThat(responseHeader).isNotEmpty();

        Collection<String> emptylist = new ArrayList<>();
        header.put("content-encoding", emptylist);
        MultiValueMap<String, String> responseHeader1 = JsonFeignResponseUtil.convertHeaders(header);

        assertThat(responseHeader1.get("content-encoding")).isEmpty();
    }

    @Test
    public void test_toResponseEntity_with_payload_not_empty() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(
                "{\"pcqId\": [\"c4402c47-c6dc-459e-884e-8f546781a5ab\"," +
                        "\"67b4161f-dd1e-43ab-9511-d4161817e1d2\"], \"responseStatus\": \"Success\"," +
                        "\"responseStatusCode\": \"200\"}",
                UTF_8).request(mock(Request.class)).build();
        ResponseEntity entity = JsonFeignResponseUtil.toResponseEntity(response, PcqWithoutCaseResponse.class);

        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        assertThat(((PcqWithoutCaseResponse) entity.getBody()).getPcqId()).contains("67b4161f-dd1e-43ab-9511-d4161817e1d2",
                "c4402c47-c6dc-459e-884e-8f546781a5ab");
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseUtil> constructor = JsonFeignResponseUtil.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
