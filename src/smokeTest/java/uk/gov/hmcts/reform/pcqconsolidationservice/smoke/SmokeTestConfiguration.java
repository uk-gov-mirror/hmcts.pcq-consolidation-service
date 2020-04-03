package uk.gov.hmcts.reform.pcqconsolidationservice.smoke;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.pcqconsolidationservice.smoke")
@PropertySource("application.properties")
public class SmokeTestConfiguration {
}
