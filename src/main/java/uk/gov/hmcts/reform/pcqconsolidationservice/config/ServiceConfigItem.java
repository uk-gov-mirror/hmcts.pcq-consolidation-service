package uk.gov.hmcts.reform.pcqconsolidationservice.config;

import java.util.List;
import javax.validation.constraints.NotNull;

public class ServiceConfigItem {

    @NotNull
    private String service;

    @NotNull
    private String jurisdiction;

    private List<String> caseTypeIds;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public List<String> getCaseTypeIds() {
        return caseTypeIds;
    }

}
