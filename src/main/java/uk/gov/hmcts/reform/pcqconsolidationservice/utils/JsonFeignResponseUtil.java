package uk.gov.hmcts.reform.pcqconsolidationservice.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;


@SuppressWarnings("unchecked")
public final class JsonFeignResponseUtil {
    private static final ObjectMapper JSON = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonFeignResponseUtil() {

    }

    public static Optional decode(Response response, Class clazz) throws IOException {
        return Optional.of(JSON.readValue(response.body().asReader(UTF_8), clazz));
    }

    public static ResponseEntity toResponseEntity(Response response, Class clazz) throws IOException {
        Optional payload = decode(response, clazz);

        return new ResponseEntity(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e -> {
            if (!("request-context".equalsIgnoreCase(e.getKey()) || "x-powered-by".equalsIgnoreCase(e.getKey())
                    || "content-length".equalsIgnoreCase(e.getKey()))) {
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        });

        return responseEntityHeaders;
    }
}
