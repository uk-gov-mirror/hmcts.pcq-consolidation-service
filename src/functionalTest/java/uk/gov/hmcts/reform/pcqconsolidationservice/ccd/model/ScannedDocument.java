package uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScannedDocument {

    public final String fileName;
    public final String controlNumber;
    public final String type;
    public final String subtype;

    @JsonProperty("exceptionRecordReference")
    public final String exceptionReference;

    @JsonProperty("url")
    public final CcdDocument url;

    public final LocalDateTime scannedDate;
    public final LocalDateTime deliveryDate;

}
