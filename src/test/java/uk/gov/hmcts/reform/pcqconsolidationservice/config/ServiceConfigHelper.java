package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import java.util.List;

public class ServiceConfigHelper {
    public static ServiceConfigProvider serviceConfigProvider(List<ServiceConfigItem> services) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setServices(services);
        return new ServiceConfigProvider(serviceConfiguration);
    }

    public static ServiceConfigItem serviceConfigItem(String service, String jurisdiction, List<String> caseTypeIds) {
        ServiceConfigItem serviceConfigItem = new ServiceConfigItem();
        serviceConfigItem.setService(service);
        serviceConfigItem.setJurisdiction(jurisdiction);
        serviceConfigItem.setCaseTypeIds(caseTypeIds);
        return serviceConfigItem;
    }
}
