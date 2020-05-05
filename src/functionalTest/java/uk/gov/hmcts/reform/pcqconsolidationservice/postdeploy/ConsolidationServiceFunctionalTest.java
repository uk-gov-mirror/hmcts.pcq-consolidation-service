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
import uk.gov.hmcts.reform.pcqconsolidationservice.model.PcqAnswerResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
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

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteMethod() throws IOException, IllegalAccessException {
        // Create the test answers records.
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
        Map<String,String[]> statusMap = (Map<String, String[]>)mapField.get(consolidationComponent);
        assertNotNull("Status Map is null", statusMap);
        String[] pcqIds = statusMap.get("PCQ_ID_FOUND");
        assertTrue("The pcqRecord 1 is not found.", Arrays.asList(pcqIds).contains(pcqRecord1));
        assertTrue("The pcqRecord 2 is not found.", Arrays.asList(pcqIds).contains(pcqRecord2));
        assertFalse("The pcqRecord 3 is found.", Arrays.asList(pcqIds).contains(pcqRecord3));

        //Check that the API - addCaseForPcq has been called and that the test records are updated.
        String[] pcqIdsProcessed = statusMap.get("PCQ_ID_PROCESSED");
        assertTrue("The pcqRecord 1 is not processed.", Arrays.asList(pcqIdsProcessed).contains(pcqRecord1));
        assertTrue("The pcqRecord 2 is not processed.", Arrays.asList(pcqIdsProcessed).contains(pcqRecord2));

        //Make a call to the getAnswer from pcq backend to verify that case Id has been updated.
        PcqAnswerResponse answerResponse = getTestAnswerRecord(pcqRecord1, pcqBackendUrl, jwtSecretKey);
        assertNotNull("The get response is null", answerResponse);
        assertNotNull("The case id is null", answerResponse.getCaseId());

        answerResponse = getTestAnswerRecord(pcqRecord2, pcqBackendUrl, jwtSecretKey);
        assertNotNull("The get response is null", answerResponse);
        assertNotNull("The case id is null", answerResponse.getCaseId());

    }


    private String createTestAnswerRecordWithoutCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswer.json", pcqBackendUrl, jwtSecretKey);
    }

    private String createTestAnswerRecordWithCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswerWithCase.json", pcqBackendUrl, jwtSecretKey);
    }
}
