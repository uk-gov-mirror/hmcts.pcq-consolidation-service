package uk.gov.hmcts.reform.pcqconsolidationservice.postdeploy;

import com.gilecode.reflection.ReflectionAccessUtils;
import com.gilecode.reflection.ReflectionAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.pcqconsolidationservice.ConsolidationComponent;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.TestApplicationConfiguration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
@TestPropertySource("classpath:application-functional.yaml")
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

        Field mapField = ReflectionUtils.findField(ConsolidationComponent.class, "pcqIdsMap");
        ReflectionAccessor accessor = ReflectionAccessUtils.getReflectionAccessor();
        accessor.makeAccessible(mapField);

        Map<String,String[]> statusMap = (Map<String, String[]>)mapField.get(consolidationComponent);
        assertNotNull("Status Map is null", statusMap);
        String[] pcqIds = statusMap.get("PCQ_ID_FOUND");
        assertTrue("The pcqRecord 1 is not found.", Arrays.asList(pcqIds).contains(pcqRecord1));
        assertTrue("The pcqRecord 2 is not found.", Arrays.asList(pcqIds).contains(pcqRecord2));
        assertFalse("The pcqRecord 3 is found.", Arrays.asList(pcqIds).contains(pcqRecord3));

    }


    private String createTestAnswerRecordWithoutCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswer.json", pcqBackendUrl, jwtSecretKey);
    }

    private String createTestAnswerRecordWithCase() throws IOException {
        return createTestAnswerRecord("JsonTestFiles/FirstSubmitAnswerWithCase.json", pcqBackendUrl, jwtSecretKey);
    }
}
