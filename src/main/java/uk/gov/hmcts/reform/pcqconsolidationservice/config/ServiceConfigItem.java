package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import java.util.List;
import javax.validation.constraints.NotNull;

public class ServiceConfigItem {

    @NotNull
    private String service;

    @NotNull
    private List<String> caseTypeIds;

    private List<CaseFieldMapping> caseFieldMappings;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getCaseTypeIds() {
        return caseTypeIds;
    }

    public List<CaseFieldMapping> getCaseFieldMappings() {
        return caseFieldMappings;
    }

    public String getCaseField(String actor) {
        CaseFieldMapping caseFieldMapping = this.caseFieldMappings.stream()
                .filter(a -> actor.equalsIgnoreCase(a.getActor()))
                .findAny()
                .orElse(null);
        return null == caseFieldMapping ? null : caseFieldMapping.getName();
    }

    public void setCaseTypeIds(List<String> caseTypeIds) {
        this.caseTypeIds = caseTypeIds;
    }

    public void setCaseFieldMappings(List<CaseFieldMapping> caseFieldMappings) {
        this.caseFieldMappings = caseFieldMappings;
    }

}
