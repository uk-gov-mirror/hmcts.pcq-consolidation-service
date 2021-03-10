package uk.gov.hmcts.reform.pcqconsolidationservice.postdeploy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model.CcdCollectionElement;
import uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model.PcqQuestions;
import uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model.ScannedDocument;
import uk.gov.hmcts.reform.pcqconsolidationservice.ccd.util.CaseCreator;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
public class ConsolidationServiceTestBase {

    private static final String COMPLETED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String OPT_OUT_YES = "Y";

    @Autowired
    private CaseCreator caseCreator;

    protected void createTestAnswerRecord(String fileName, String apiUrl, String pcqId, String jwtSecretKey)
            throws IOException {
        String jsonString = jsonStringFromFile(fileName);
        PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonString);

        pcqAnswerRequest.setPcqId(pcqId);
        pcqAnswerRequest.setCompletedDate(updateCompletedDate(pcqAnswerRequest.getCompletedDate()));

        postRequestPcqBackend(apiUrl, pcqAnswerRequest, jwtSecretKey);
    }

    protected void removeTestAnswerRecord(String fileName, String apiUrl, String pcqId, String jwtSecretKey)
            throws IOException {
        String jsonString = jsonStringFromFile(fileName);
        PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonString);

        pcqAnswerRequest.setPcqId(pcqId);
        pcqAnswerRequest.setOptOut(OPT_OUT_YES);
        pcqAnswerRequest.setCompletedDate(updateCompletedDate(pcqAnswerRequest.getCompletedDate()));

        postRequestPcqBackend(apiUrl, pcqAnswerRequest, jwtSecretKey);
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

    @SuppressWarnings("unchecked")
    protected CaseDetails createCcdPcqQuestionsPaperCase(String title, String dcn) {
        Optional<CaseDetails> caseDetails = caseCreator.findCase(title);
        if (caseDetails.isEmpty()) {
            CcdCollectionElement<ScannedDocument> scannedDoc =
                    new CcdCollectionElement(caseCreator.createScannedDocument(dcn));
            List<CcdCollectionElement<ScannedDocument>> scannedDocumentList = Collections.singletonList(scannedDoc);
            PcqQuestions pcqQuestions = PcqQuestions.builder()
                    .text(title)
                    .pcqId(null)
                    .scannedDocuments(scannedDocumentList)
                    .build();
            return caseCreator.createCase(pcqQuestions);
        } else {
            return caseDetails.get();
        }
    }

    @SuppressWarnings("unchecked")
    protected CaseDetails createCcdPcqQuestionsDigitalCase(String title, String pcqId) {
        Optional<CaseDetails> caseDetails = caseCreator.findCase(title);
        if (caseDetails.isEmpty()) {
            List<CcdCollectionElement<ScannedDocument>> scannedDocumentList = Collections.EMPTY_LIST;
            PcqQuestions pcqQuestions = PcqQuestions.builder()
                    .text(title)
                    .pcqId(pcqId)
                    .scannedDocuments(scannedDocumentList)
                    .build();
            return caseCreator.createCase(pcqQuestions);
        } else {
            return caseDetails.get();
        }
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
