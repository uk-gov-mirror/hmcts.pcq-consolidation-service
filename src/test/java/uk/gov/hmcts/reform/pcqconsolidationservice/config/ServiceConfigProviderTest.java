package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ServiceNotConfiguredException;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigHelper.serviceConfigProvider;


class ServiceConfigProviderTest {

    @Test
    @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
    public void configShouldReturnTheRightServiceConfigurationWhenPresent() {
        // given
        ServiceConfigItem service1Config =
                ServiceConfigHelper.serviceConfigItem(
                        "service1",
                        Arrays.asList("ctid1", "ctid2"),
                        singletonList(ServiceConfigHelper.createCaseFieldMap("ACTOR_1", "pcqId1")));

        ServiceConfigItem service2Config =
                ServiceConfigHelper.serviceConfigItem(
                        "service2",
                        Arrays.asList("ctid1", "ctid3"),
                        singletonList(ServiceConfigHelper.createCaseFieldMap("ACTOR_2", "pcqId2")));

        List<ServiceConfigItem> configuredServices = Arrays.asList(service1Config, service2Config);

        // when
        ServiceConfigItem configItem = serviceConfigProvider(configuredServices).getConfig("service2");

        // then
        assertThat(configItem).isEqualToComparingFieldByField(service2Config);
    }

    @Test
    @SuppressWarnings({"PMD.UnnecessaryFullyQualifiedName","PMD.DataflowAnomalyAnalysis"})
    public void configShouldThrowExceptionWhenServiceIsNotConfigured() {
        ServiceConfigProvider serviceConfigProvider = serviceConfigProvider(
                Arrays.asList(
                        ServiceConfigHelper.serviceConfigItem(
                                "service",
                                singletonList("ctid"),
                                singletonList(ServiceConfigHelper.createCaseFieldMap("ACTOR_2", "pcqId2")))
                )
        );

        assertThatThrownBy(
            () -> serviceConfigProvider.getConfig("non-existing-service")
        )
                .isInstanceOf(ServiceNotConfiguredException.class)
                .hasMessage("Service non-existing-service is not configured");
    }
}
