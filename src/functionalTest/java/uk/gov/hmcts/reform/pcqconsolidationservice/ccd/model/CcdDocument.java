package uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CcdDocument {

    @JsonProperty("document_url")
    public final String documentUrl;

}
