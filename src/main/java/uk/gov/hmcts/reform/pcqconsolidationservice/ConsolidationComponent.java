package uk.gov.hmcts.reform.pcqconsolidationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ServiceNotConfiguredException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConsolidationComponent {

    private final Map<String, PcqAnswerResponse[]> pcqIdsMap = new ConcurrentHashMap<>();

    @Autowired
    private CcdClientApi ccdClientApi;

    @Autowired
    private PcqBackendService pcqBackendService;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @SuppressWarnings({"unchecked", "PMD.DataflowAnomalyAnalysis"})
    public void execute() {
        try {
            log.info("ConsolidationComponent started");

            // Step 1. Get the list of PCQs without Case Id.
            ResponseEntity<PcqRecordWithoutCaseResponse> responseEntity = pcqBackendService.getPcqWithoutCase();
            PcqRecordWithoutCaseResponse pcqWithoutCaseResponse = responseEntity.getBody();
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                processPcqRecordsWithoutCase(pcqWithoutCaseResponse);

            } else {
                if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST || responseEntity.getStatusCode()
                        == HttpStatus.INTERNAL_SERVER_ERROR) {
                    if (pcqWithoutCaseResponse == null || pcqWithoutCaseResponse.getResponseStatus() == null) {
                        log.error("Response from backend service invalid, missing body");
                    } else {
                        log.error("PcqWithoutCase API generated error message {} ",
                                pcqWithoutCaseResponse.getResponseStatus());
                    }
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

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis","PMD.ConfusingTernary"})
    private void processPcqRecordsWithoutCase(PcqRecordWithoutCaseResponse pcqWithoutCaseResponse) {
        if (pcqWithoutCaseResponse == null || pcqWithoutCaseResponse.getPcqRecord().length == 0) {
            log.info("Pcq Ids, without case information, are not found");

        } else {
            pcqIdsMap.put("PCQ_ID_FOUND", pcqWithoutCaseResponse.getPcqRecord());
            for (PcqAnswerResponse pcqAnswerResponse : pcqWithoutCaseResponse.getPcqRecord()) {
                Long caseReference = null;

                //Step 2, Check for DCN, if available invoke Elastic Search API to get the case Id.
                if (pcqAnswerResponse.getDcnNumber() != null && !pcqAnswerResponse.getDcnNumber().isEmpty()) {
                    caseReference = findCaseReferenceFromDcn(
                            pcqAnswerResponse.getDcnNumber(),
                            pcqAnswerResponse.getServiceId());

                } else if (pcqAnswerResponse.getPcqId() != null && !pcqAnswerResponse.getPcqId().isEmpty()) {
                    //Step 3, No DCN so invoke Elastic Search API on pcqId to get the case Id.
                    caseReference = findCaseReferenceFromPcqId(
                            pcqAnswerResponse.getPcqId(),
                            pcqAnswerResponse.getServiceId(),
                            pcqAnswerResponse.getActor());
                }

                if (caseReference != null) {
                    //Step 4, Invoke the addCaseForPcq API to update the case id for the Pcq.
                    invokeAddCaseForPcq(pcqAnswerResponse.getPcqId(), caseReference.toString());
                }
            }
            pcqIdsMap.put("PCQ_ID_PROCESSED", pcqWithoutCaseResponse.getPcqRecord());
        }
    }

    private Long findCaseReferenceFromPcqId(String pcqId, String serviceId, String actor) {
        try {
            ServiceConfigItem serviceConfigItemByServiceId = serviceConfigProvider.getConfig(serviceId);
            List<Long> caseReferences
                    = ccdClientApi.getCaseRefsByPcqId(pcqId, serviceConfigItemByServiceId.getService(), actor);

            if (caseReferences != null && caseReferences.size() == 1) {
                Long caseReferenceForPcq = caseReferences.get(0);
                log.info("Found {} case reference {} for PCQ ID {}", serviceId, caseReferenceForPcq, pcqId);
                return caseReferenceForPcq;
            } else {
                log.info("Unable to find {} case reference for PCQ ID {}", serviceId, pcqId);
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for PCQ ID {} as no {} configuration was found", pcqId, serviceId);
        }

        return null;
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
    private Long findCaseReferenceFromDcn(String dcn, String serviceId) {
        Long caseReferenceForPcq = null;

        try {
            ServiceConfigItem serviceConfigItemByServiceId = serviceConfigProvider.getConfig(serviceId);
            List<Long> caseReferences
                    = ccdClientApi.getCaseRefsByOriginatingFormDcn(dcn, serviceConfigItemByServiceId.getService());

            if (caseReferences != null && caseReferences.size() == 1) {
                caseReferenceForPcq = caseReferences.get(0);
                log.info("Found {} case reference {} for DCN {}", serviceId, caseReferenceForPcq, dcn);
            } else {
                log.info("Unable to find {} case reference for DCN {}", serviceId, dcn);
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for DCN {} as no {} configuration was found", dcn, serviceId);
        }

        return caseReferenceForPcq;
    }

    @SuppressWarnings({"unchecked"})
    private void invokeAddCaseForPcq(String pcqId, String caseId) {
        ResponseEntity<SubmitResponse> responseEntity = pcqBackendService.addCaseForPcq(pcqId, caseId);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully added case {} for PCQ ID {}", caseId, pcqId);

        } else {
            if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST || responseEntity.getStatusCode()
                    == HttpStatus.INTERNAL_SERVER_ERROR) {
                SubmitResponse submitResponse = responseEntity.getBody();
                if (submitResponse == null || submitResponse.getResponseStatus() == null) {
                    log.error("Response from backend service invalid, missing body");
                } else {
                    log.error("AddCaseForPcq API generated error message {}", submitResponse.getResponseStatus());
                }
            } else {
                log.error("AddCaseForPcq API generated unknown error message");
            }
        }
    }
}
