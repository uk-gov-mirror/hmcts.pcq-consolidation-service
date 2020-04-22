package uk.gov.hmcts.reform.pcqconsolidationservice.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.pcqconsolidationservice.ConsolidationComponent;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.TestApplicationConfiguration;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign.PcqBackendFeignClient;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.impl.PcqBackendServiceImpl;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod", "PMD.AbstractClassWithoutAbstractMethod"})
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected PcqBackendServiceImpl pcqBackendServiceImpl;

    @Autowired
    protected PcqBackendFeignClient pcqBackendFeignClient;

    @Autowired
    protected ConsolidationComponent consolidationComponent;

}
