package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ServiceNotConfiguredException;

import java.util.Locale;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
@EnableConfigurationProperties(ServiceConfiguration.class)
public class ServiceConfigProvider {

    private final Map<String, ServiceConfigItem> servicesByName;

    public ServiceConfigProvider(ServiceConfiguration serviceConfiguration) {
        this.servicesByName =
                serviceConfiguration
                        .getServices()
                        .stream()
                        .collect(
                                toMap(
                                        ServiceConfigItem::getService,
                                        identity()
                                )
                        );
    }

    public ServiceConfigItem getConfig(String service) {
        ServiceConfigItem configItem = servicesByName.get(service.toUpperCase(Locale.ENGLISH));

        if (configItem == null) {
            throw new ServiceNotConfiguredException(String.format("Service %s is not configured", service));
        }

        return configItem;
    }

}

