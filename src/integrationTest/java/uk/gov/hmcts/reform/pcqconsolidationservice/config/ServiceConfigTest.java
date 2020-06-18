package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ServiceNotConfiguredException;
import uk.gov.hmcts.reform.pcqconsolidationservice.util.SpringBootIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(locations = "/application.properties")
class ServiceConfigTest extends SpringBootIntegrationTest {

    private static final String TEST_SERVICE_NAME = "pcqtestone";
    private static final String TEST_SERVICE_UNKNOWN_NAME = "madeupname";
    private static final String TEST_CASE_TYPE_ID_0 = "CaseTypeA";
    private static final String TEST_CASE_TYPE_ID_1 = "CaseTypeB";
    private static final int TEST_CASE_TYPES_SIZE = 2;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Test
    public void serviceConfigItemShouldCaptureServiceDetails() {
        ServiceConfigItem configItem = serviceConfigProvider.getConfig(TEST_SERVICE_NAME);

        assertEquals(TEST_SERVICE_NAME, configItem.getService(),"Service name is correct");
        assertEquals(TEST_CASE_TYPES_SIZE, configItem.getCaseTypeIds().size(), "Correct number of case types");
        assertEquals(TEST_CASE_TYPE_ID_0, configItem.getCaseTypeIds().get(0), "First case type correct");
        assertEquals(TEST_CASE_TYPE_ID_1, configItem.getCaseTypeIds().get(1), "Second case type correct");
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Test
    public void serviceConfigItemShouldThrowExeceptionIfMissing() {
        Exception exception = assertThrows(ServiceNotConfiguredException.class, () -> {
            serviceConfigProvider.getConfig(TEST_SERVICE_UNKNOWN_NAME);
        });

        String expectedMessage = "Service madeupname is not configured";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "Expected error message for service not found");
    }
}
