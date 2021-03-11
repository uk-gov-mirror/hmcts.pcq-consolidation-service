package uk.gov.hmcts.reform.pcqconsolidationservice.ccd.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@Getter
@Setter
public class PcqQuestions {

    private String pcqId;
    private String text;
    private String judgeNotes;
    public final List<CcdCollectionElement<ScannedDocument>> scannedDocuments;

}
