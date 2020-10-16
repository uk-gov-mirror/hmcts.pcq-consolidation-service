package uk.gov.hmcts.reform.pcqconsolidationservice.postdeploy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.generateUuid;


@Slf4j
public class ConsolidationServiceTestBase {

    private static final String COMPLETED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected String createTestAnswerRecord(String fileName, String apiUrl, String jwtSecretKey) throws IOException {
        String jsonString = jsonStringFromFile(fileName);
        PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonString);

        String randomPcqId = generateUuid();
        pcqAnswerRequest.setPcqId(randomPcqId);
        pcqAnswerRequest.setCompletedDate(updateCompletedDate(pcqAnswerRequest.getCompletedDate()));

        postRequestPcqBackend(apiUrl, pcqAnswerRequest, jwtSecretKey);

        return randomPcqId;

    }

    protected PcqAnswerResponse getTestAnswerRecord(String pcqId, String apiUrl, String secretKey) throws IOException {
        return getResponseFromBackend(apiUrl, pcqId, secretKey);
    }

    private void postRequestPcqBackend(String apiUrl, PcqAnswerRequest requestObject, String secretKey) {
        WebClient pcqWebClient = createPcqBackendWebClient(apiUrl, secretKey);
        WebClient.RequestHeadersSpec requestBodySpec = pcqWebClient.post().uri(URI.create(
                apiUrl + "/pcq/backend/submitAnswers")).body(BodyInserters.fromValue(requestObject));
        Map response3 = requestBodySpec.retrieve().bodyToMono(Map.class).block();
        log.info("Returned response " + response3.toString());
    }

    private PcqAnswerResponse getResponseFromBackend(String apiUrl, String pcqId, String secretKey) {
        WebClient pcqWebClient = createPcqBackendWebClient(apiUrl, secretKey);
        WebClient.RequestHeadersSpec requestBodySpec = pcqWebClient.get().uri(URI.create(
                apiUrl + "/pcq/backend/getAnswer/" + pcqId));
        PcqAnswerResponse response3 = requestBodySpec.retrieve().bodyToMono(PcqAnswerResponse.class).block();
        log.info("Returned response " + response3.toString());
        return response3;
    }

    private WebClient createPcqBackendWebClient(String apiUrl, String secretKey) {
        return WebClient
                .builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Correlation-Id", "Pcq Consolidation Functional Test")
                .defaultHeader("Authorization", "Bearer "
                        + PcqUtils.generateAuthorizationToken(secretKey, "TEST", "TEST_AUTHORITY"))
                .defaultUriVariables(Collections.singletonMap("url", apiUrl))
                .build();
    }

    private String updateCompletedDate(String completedDateStr) {
        Timestamp completedTime = PcqUtils.getTimeFromString(completedDateStr);
        Calendar calendar = Calendar.getInstance();
        completedTime.setTime(calendar.getTimeInMillis());
        return convertTimeStampToString(completedTime);
    }

    private String convertTimeStampToString(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(COMPLETED_DATE_FORMAT, Locale.UK);
        return dateFormat.format(timestamp);
    }
}
