package uk.gov.hmcts.reform.pcqconsolidationservice.controller.response;

import java.io.Serializable;
import java.util.Arrays;

public class PcqRecordWithoutCaseResponse implements Serializable {
    public static final long serialVersionUID = 432943329;

    private static final PcqAnswerResponse[] EMPTY_PCQ_ANSWER_RESPONSE = {};

    private PcqAnswerResponse[] pcqRecord;

    private String responseStatus;

    private String responseStatusCode;

    public PcqAnswerResponse[] getPcqRecord() {
        if (pcqRecord == null) {
            return EMPTY_PCQ_ANSWER_RESPONSE;
        }
        return Arrays.copyOf(pcqRecord, pcqRecord.length);
    }

    @SuppressWarnings("PMD.UseVarargs")
    public void setPcqRecord(PcqAnswerResponse[] pcqRecord) {
        if (pcqRecord == null) {
            this.pcqRecord = new PcqAnswerResponse[0];
        } else {
            this.pcqRecord = Arrays.copyOf(pcqRecord, pcqRecord.length);
        }
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }
}
