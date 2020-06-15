package uk.gov.hmcts.reform.pcqconsolidationservice.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings({"PMD.TooManyFields"})
public class PcqAnswerResponse implements Serializable {

    public static final long serialVersionUID = 1328743L;

    @JsonProperty("pcqId")
    private String pcqId;

    @JsonProperty("ccdCaseId")
    private String caseId;

    @JsonProperty("partyId")
    private String partyId;

    @JsonProperty("channel")
    private Integer channel;

    @JsonProperty("completedDate")
    private String completedDate;

    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("actor")
    private String actor;

    @JsonProperty("versionNo")
    private Integer versionNo;

    private PcqAnswers pcqAnswers;

}
