package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import java.util.List;

public final class ServiceConfigHelper {
    private ServiceConfigHelper() {
    }

    public static ServiceConfigProvider serviceConfigProvider(List<ServiceConfigItem> services) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setServices(services);
        return new ServiceConfigProvider(serviceConfiguration);
    }

    public static ServiceConfigItem serviceConfigItem(
            String service,
            List<String> caseTypeIds,
            List<CaseFieldMapping> caseFieldMappings) {
        ServiceConfigItem serviceConfigItem = new ServiceConfigItem();
        serviceConfigItem.setService(service);
        serviceConfigItem.setCaseTypeIds(caseTypeIds);
        serviceConfigItem.setCaseFieldMappings(caseFieldMappings);
        return serviceConfigItem;
    }

    public static CaseFieldMapping createCaseFieldMap(String actor, String name) {
        CaseFieldMapping caseFieldMapping = new CaseFieldMapping();
        caseFieldMapping.setActor(actor);
        caseFieldMapping.setActor(name);
        return caseFieldMapping;
    }
}
