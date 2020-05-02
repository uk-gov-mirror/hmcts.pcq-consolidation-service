package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class ServiceConfigTest {

    private static final String TEST_SERVICE_NAME = "pcqtestone";

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Test
    public void serviceConfigItemShouldCaptureServiceDetails() {
        ServiceConfigItem configItem = serviceConfigProvider.getConfig(TEST_SERVICE_NAME);

        Assert.assertEquals(TEST_SERVICE_NAME, configItem.getService());
    }
}
