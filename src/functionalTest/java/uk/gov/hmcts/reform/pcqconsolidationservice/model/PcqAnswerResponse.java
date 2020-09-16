package uk.gov.hmcts.reform.pcqconsolidationservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This is the object representing the REST request for the SubmitAnswers API.
 */
public class PcqAnswerResponse implements Serializable {

    public static final long serialVersionUID = 4328743L;

    private String pcqId;

    @JsonProperty("ccdCaseId")
    private String caseId;

    private String partyId;

    private String dcnNumber;

    private int channel;

    private String completedDate;

    private String serviceId;

    private String actor;

    private int versionNo;

    private PcqAnswers pcqAnswers;

    public PcqAnswerResponse() {
        // Intentionally left blank.
    }

    public PcqAnswerResponse(String pcqId) {

        this.pcqId = pcqId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getDcnNumber() {
        return dcnNumber;
    }

    public void setDcnNumber(String dcnNumber) {
        this.dcnNumber = dcnNumber;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public PcqAnswers getPcqAnswers() {
        return pcqAnswers;
    }

    public void setPcqAnswers(PcqAnswers pcqAnswers) {
        this.pcqAnswers = pcqAnswers;
    }

    public String getPcqId() {
        return pcqId;
    }

    public void setPcqId(String pcqId) {
        this.pcqId = pcqId;
    }
}
