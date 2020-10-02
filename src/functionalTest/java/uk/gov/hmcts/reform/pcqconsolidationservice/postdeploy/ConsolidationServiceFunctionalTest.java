package uk.gov.hmcts.reform.pcqconsolidationservice.postdeploy;

import com.gilecode.reflection.ReflectionAccessUtils;
import com.gilecode.reflection.ReflectionAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.pcqconsolidationservice.ConsolidationComponent;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.TestApplicationConfiguration;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
@ActiveProfiles("functional")
@Slf4j
public class ConsolidationServiceFunctionalTest extends ConsolidationServiceTestBase {

    @Value("${pcqBackendUrl}")
    private String pcqBackendUrl;

    @Value("${jwt_test_secret}")
    private String jwtSecretKey;

    @Autowired
    private ConsolidationComponent consolidationComponent;

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteMethod() throws IOException, IllegalAccessException {

        // Create the test answer records.
        final String pcqRecord1 = createTestAnswerRecordWithoutCase();
        final String pcqRecord2 = createTestAnswerRecordWithoutCase();
        final String pcqRecord3 = createTestAnswerRecordWithCase();

        //Invoke the executor
        consolidationComponent.execute();

        //Make the status map accessible from the ConsolidationComponent.
        Field mapField = ReflectionUtils.findField(ConsolidationComponent.class, "pcqIdsMap");
        ReflectionAccessor accessor = ReflectionAccessUtils.getReflectionAccessor();
        accessor.makeAccessible(mapField);

        //Check that the API - pcqWithoutCase has been called and that the test records are found.
        Map<String, PcqAnswerResponse[]> statusMap = (Map<String, PcqAnswerResponse[]>)mapField.get(
                consolidationComponent);
        assertNotNull("Status Map is null", statusMap);
        PcqAnswerResponse[] pcqAnswerRecords = statusMap.get("PCQ_ID_FOUND");
        assertPcqIdsRetrieved(pcqAnswerRecords, pcqRecord1, pcqRecord2, pcqRecord3);

        //Check that the API - addCaseForPcq has been called and that the test records are updated.
        PcqAnswerResponse[] pcqRecordsProcessed = statusMap.get("PCQ_ID_PROCESSED");
        assertPcqIdsProcessed(pcqRecordsProcessed, pcqRecord1, pcqRecord2);

        //Make a call to the getAnswer from pcq backend to verify that case Id has been updated.
        PcqAnswerResponse answerResponse = getTestAnswerRecord(pcqRecord1, pcqBackendUrl, jwtSecretKey);
        assertNotNull("The get response is null", answerResponse);

        answerResponse = getTestAnswerRecord(pcqRecord2, pcqBackendUrl, jwtSecretKey);
        assertNotNull("The get response is null", answerResponse);
    }

    private String createTestAnswerRecordWithoutCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswer.json", pcqBackendUrl, jwtSecretKey);
    }

    private String createTestAnswerRecordWithCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswerWithCase.json", pcqBackendUrl, jwtSecretKey);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private void assertPcqIdsRetrieved(PcqAnswerResponse[] pcqAnswerRecords, String pcqRecord1, String pcqRecord2,
                                       String pcqRecord3) {
        List<String> pcqIds = new ArrayList<>();
        for (PcqAnswerResponse answerResponse : pcqAnswerRecords) {
            pcqIds.add(answerResponse.getPcqId());
        }

        assertTrue("The pcqRecord 1 is not found.", pcqIds.contains(pcqRecord1));
        assertTrue("The pcqRecord 2 is not found.", pcqIds.contains(pcqRecord2));
        assertFalse("The pcqRecord 3 is found.", pcqIds.contains(pcqRecord3));
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private void assertPcqIdsProcessed(PcqAnswerResponse[] pcqAnswerRecords, String pcqRecord1, String pcqRecord2) {
        List<String> pcqIds = new ArrayList<>();
        for (PcqAnswerResponse answerResponse : pcqAnswerRecords) {
            pcqIds.add(answerResponse.getPcqId());
        }
        assertTrue("The pcqRecord 1 is not processed.", pcqIds.contains(pcqRecord1));
        assertTrue("The pcqRecord 2 is not processed.", pcqIds.contains(pcqRecord2));
    }
}
