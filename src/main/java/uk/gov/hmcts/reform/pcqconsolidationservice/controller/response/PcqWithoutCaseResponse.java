package uk.gov.hmcts.reform.pcqconsolidationservice.controller.response;

import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Arrays;

/**
 * This is the object representing the REST response for the PCQWithoutACase API.
 */
@NoArgsConstructor
public class PcqWithoutCaseResponse implements Serializable {

    public static final long serialVersionUID = 432973322;

    private String[] pcqId;

    private String responseStatus;

    private String responseStatusCode;

    public String[] getPcqId() {
        if (pcqId == null) {
            return null;
        }
        return Arrays.copyOf(pcqId, pcqId.length);
    }

    @SuppressWarnings("PMD.UseVarargs")
    public void setPcqId(String[] pcqId) {
        if (pcqId == null) {
            this.pcqId = new String[0];
        } else {
            this.pcqId = Arrays.copyOf(pcqId, pcqId.length);
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