package uk.gov.hmcts.reform.pcqconsolidationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcq.commons.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ServiceNotConfiguredException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ConsolidationComponent {

    private final Map<String, PcqAnswerResponse[]> pcqIdsMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> serviceSummaryMap = new ConcurrentHashMap<>();

    private static final String CR_STRING = "\r\n";
    private static final String TAB_STRING = "\t| ";

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
            logSummary();

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
                incrementServiceCount(serviceId + "_online_channel_matched");
                return caseReferenceForPcq;
            } else {
                log.info("Unable to find {} case reference for PCQ ID {}", serviceId, pcqId);
                incrementServiceCount(serviceId + "_online_channel_not_found");
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for PCQ ID {} as no {} configuration was found", pcqId, serviceId);
            incrementServiceCount(serviceId + "_online_channel_error");
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
                incrementServiceCount(serviceId + "_paper_channel_matched");
            } else {
                log.info("Unable to find {} case reference for DCN {}", serviceId, dcn);
                incrementServiceCount(serviceId + "_paper_channel_not_found");
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for DCN {} as no {} configuration was found", dcn, serviceId);
            incrementServiceCount(serviceId + "_paper_channel_error");
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

    private void incrementServiceCount(String service) {

        if (serviceSummaryMap.get(service) == null) {
            serviceSummaryMap.put(service, 1);
        } else {
            int count = serviceSummaryMap.get(service) + 1;
            serviceSummaryMap.put(service, count);
        }
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void logSummary() {

        StringBuilder stringBuilder = new StringBuilder("\r\nConsolidation Service Case Matching Summary : ");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy", Locale.UK);
        stringBuilder.append(dateFormat.format(new Date()))
                .append(CR_STRING)
                .append("Service\t\t\t\t\t Matched | Not Found | Errors\r\n")
                .append("-----------------------------------------------------------")
                .append(CR_STRING);
        AtomicInteger totalOnlineMatched = new AtomicInteger();
        AtomicInteger totalOnlineNotFound = new AtomicInteger();
        AtomicInteger totalOnlineError = new AtomicInteger();
        AtomicInteger totalPaperMatched = new AtomicInteger();
        AtomicInteger totalPaperNotFound = new AtomicInteger();
        AtomicInteger totalPaperError = new AtomicInteger();
        Set<String> serviceKeySet = serviceConfigProvider.getServiceNames();

        serviceKeySet.forEach((service) -> {
            stringBuilder.append(service.toUpperCase(Locale.UK) + " Online Channel")
                    .append("\t");
            Integer onlineMatchedCount = serviceSummaryMap.get(service + "_online_channel_matched");
            Integer onlineNotFoundCount =  serviceSummaryMap.get(service + "_online_channel_not_found");
            Integer onlineErredCount = serviceSummaryMap.get(service + "_online_channel_error");
            stringBuilder.append(onlineMatchedCount == null ? 0 : onlineMatchedCount)
                    .append(TAB_STRING)
                    .append(onlineNotFoundCount == null ? 0 : onlineNotFoundCount)
                    .append(TAB_STRING)
                    .append(onlineErredCount == null ? 0 : onlineErredCount)
                    .append(CR_STRING)
                    .append(service.toUpperCase(Locale.UK) + " Paper Channel")
                    .append("\t");
            Integer paperMatchedCount = serviceSummaryMap.get(service + "_paper_channel_matched");
            Integer paperNotFoundCount =  serviceSummaryMap.get(service + "_paper_channel_not_found");
            Integer paperErredCount = serviceSummaryMap.get(service + "_paper_channel_error");
            stringBuilder.append(paperMatchedCount == null ? 0 : paperMatchedCount)
                    .append(TAB_STRING)
                    .append(paperNotFoundCount == null ? 0 : paperNotFoundCount)
                    .append(TAB_STRING)
                    .append(paperErredCount == null ? 0 : paperErredCount)
                    .append(CR_STRING);
            totalOnlineMatched.addAndGet(onlineMatchedCount == null ? 0 : onlineMatchedCount);
            totalOnlineNotFound.addAndGet(onlineNotFoundCount == null ? 0 : onlineNotFoundCount);
            totalOnlineError.addAndGet(onlineErredCount == null ? 0 : onlineErredCount);
            totalPaperMatched.addAndGet(paperMatchedCount == null ? 0 : paperMatchedCount);
            totalPaperNotFound.addAndGet(paperNotFoundCount == null ? 0 : paperNotFoundCount);
            totalPaperError.addAndGet(paperErredCount == null ? 0 : paperErredCount);
        });
        stringBuilder.append("Total Online ")
                .append(totalOnlineMatched.intValue())
                .append(TAB_STRING)
                .append(totalOnlineNotFound.intValue())
                .append(TAB_STRING)
                .append(totalOnlineError.intValue())
                .append(CR_STRING)
                .append("Total Paper\t ")
                .append(totalPaperMatched.intValue())
                .append(TAB_STRING)
                .append(totalPaperNotFound.intValue())
                .append(TAB_STRING)
                .append(totalPaperError.intValue())
                .append(CR_STRING)
                .append("Total \t\t ")
                .append(totalOnlineMatched.intValue() + totalPaperMatched.intValue())
                .append(TAB_STRING)
                .append(totalOnlineNotFound.intValue() + totalPaperNotFound.intValue())
                .append(TAB_STRING)
                .append(totalOnlineError.intValue() + totalPaperError.intValue());

        log.info(stringBuilder.toString());
    }

}
