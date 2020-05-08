package uk.gov.hmcts.reform.pcqconsolidationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfiguration;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConsolidationComponent {

    private final Map<String, String[]> pcqIdsMap = new ConcurrentHashMap<>();

    @Autowired
    private CcdClientApi ccdClientApi;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    @Autowired
    private PcqBackendService pcqBackendService;

    @SuppressWarnings({"unchecked", "PMD.UnusedLocalVariable", "PMD.ConfusingTernary", "PMD.DataflowAnomalyAnalysis"})
    public void execute() {
        try {
            log.info("ConsolidationComponent started");

            // Step 1. Get the list of PCQs without Case Id.
            ResponseEntity<PcqWithoutCaseResponse> responseEntity = pcqBackendService.getPcqWithoutCase();
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                PcqWithoutCaseResponse pcqWithoutCaseResponse = responseEntity.getBody();
                if (pcqWithoutCaseResponse.getPcqId() != null) {
                    pcqIdsMap.put("PCQ_ID_FOUND", pcqWithoutCaseResponse.getPcqId());
                    for (String pcqId : pcqWithoutCaseResponse.getPcqId()) {
                        //Step 2, Invoke the Elastic Search API to get the case Ids for each Pcq.
                        Long caseReference = findCaseReferenceFromPcqId(pcqId);

                        if (caseReference != null) {
                            //Step 3, Invoke the addCaseForPcq API to update the case id for the Pcq.
                            invokeAddCaseForPcq(pcqId, caseReference.toString());
                        }
                    }
                    pcqIdsMap.put("PCQ_ID_PROCESSED", pcqWithoutCaseResponse.getPcqId());
                } else {
                    log.info("Pcq Ids, without case information, are not found.");
                }
            } else {
                if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST || responseEntity.getStatusCode()
                        == HttpStatus.INTERNAL_SERVER_ERROR) {
                    log.error("PcqWithoutCase API generated error message {} ", ((PcqWithoutCaseResponse)
                            responseEntity.getBody()).getResponseStatus());
                } else {
                    log.error("PcqWithoutCase API generated unknown error message");
                }
            }

        } catch (ExternalApiException externalApiException) {
            log.error("API could not be invoked due to error message - {}", externalApiException.getErrorMessage());
            throw externalApiException;
        }
        
        log.info("ConsolidationComponent finished");
    }

    @SuppressWarnings({"unchecked","PMD.DataflowAnomalyAnalysis"})
    private Long findCaseReferenceFromPcqId(String pcqId) {
        Long caseReferenceForPcq = null;

        for (ServiceConfigItem serviceConfigItem : serviceConfiguration.getServices()) {
            List<Long> caseReferences = ccdClientApi.getCaseRefsByPcqId(pcqId, serviceConfigItem.getService());
            log.info("Iterating through known services to find case reference for {}", pcqId);
            if (caseReferences != null && caseReferences.size() == 1) {
                caseReferenceForPcq = caseReferences.get(0);
                log.info("Found case {} for pcqId {}", caseReferenceForPcq, pcqId);
                break;
            }
        }

        if (null == caseReferenceForPcq) {
            log.info("Unable to find a case for pcqId {}", pcqId);
        }

        return caseReferenceForPcq;
    }

    @SuppressWarnings("unchecked")
    private void invokeAddCaseForPcq(String pcqId, String caseId) {
        ResponseEntity<SubmitResponse> submitResponse = pcqBackendService.addCaseForPcq(pcqId, caseId);
        if (submitResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully added case information for Pcq id {} .", pcqId);
        } else {
            if (submitResponse.getStatusCode() == HttpStatus.BAD_REQUEST || submitResponse.getStatusCode()
                    == HttpStatus.INTERNAL_SERVER_ERROR) {
                log.error("AddCaseForPcq API generated error message {} ", ((SubmitResponse)
                        submitResponse.getBody()).getResponseStatus());
            } else {
                log.error("AddCaseForPcq API generated unknown error message");
            }
        }
    }

}
