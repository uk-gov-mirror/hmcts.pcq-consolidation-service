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
    private static final String TAB_STRING = "| ";
    private static final String TOTAL_ONLINE_STRING = "Total Online";
    private static final String TOTAL_PAPER_STRING = "Total Paper";
    private static final String TOTAL_STRING = "Total";
    private static final String SUMMARY_HEADING_STRING = "\r\nConsolidation Service Case Matching Summary : ";
    private static final String FORMAT_STR_LENGTH_30 = "%1$-30s";
    private static final String SERVICE_SUMMARY_STRING = String.format(FORMAT_STR_LENGTH_30, "Service")
            + "Matched | Not Found | Errors\r\n";
    private static final String ONLINE_CHANNEL_SUFFIX = " Online Channel";
    private static final String ONLINE_MATCH_SUFFIX = "_online_channel_matched";
    private static final String ONLINE_NOT_FOUND_SUFFIX = "_online_channel_not_found";
    private static final String ONLINE_ERROR_SUFFIX = "_online_channel_error";
    private static final String PAPER_CHANNEL_SUFFIX = " Paper Channel";
    private static final String PAPER_MATCH_SUFFIX = "_paper_channel_matched";
    private static final String PAPER_NOT_FOUND_SUFFIX = "_paper_channel_not_found";
    private static final String PAPER_ERROR_SUFFIX = "_paper_channel_error";
    private static final String FORMAT_STR_LENGTH_10 = "%1$-10s";
    private static final String FORMAT_STR_LENGTH_8 = "%1$-8s";

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
        } finally {
            logSummary();
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
                incrementServiceCount(serviceId + ONLINE_MATCH_SUFFIX);
                return caseReferenceForPcq;
            } else {
                log.info("Unable to find {} case reference for PCQ ID {}", serviceId, pcqId);
                incrementServiceCount(serviceId + ONLINE_NOT_FOUND_SUFFIX);
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for PCQ ID {} as no {} configuration was found", pcqId, serviceId);
            incrementServiceCount(serviceId + ONLINE_ERROR_SUFFIX);
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
                incrementServiceCount(serviceId + PAPER_MATCH_SUFFIX);
            } else {
                log.info("Unable to find {} case reference for DCN {}", serviceId, dcn);
                incrementServiceCount(serviceId + PAPER_NOT_FOUND_SUFFIX);
            }

        } catch (ServiceNotConfiguredException snce) {
            log.error("Error searching cases for DCN {} as no {} configuration was found", dcn, serviceId);
            incrementServiceCount(serviceId + PAPER_ERROR_SUFFIX);
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

    private void logSummary() {
        StringBuilder stringBuilder = new StringBuilder(getSummaryString());

        AtomicInteger totalOnlineMatched = new AtomicInteger();
        AtomicInteger totalOnlineNotFound = new AtomicInteger();
        AtomicInteger totalOnlineError = new AtomicInteger();
        AtomicInteger totalPaperMatched = new AtomicInteger();
        AtomicInteger totalPaperNotFound = new AtomicInteger();
        AtomicInteger totalPaperError = new AtomicInteger();

        stringBuilder.append(getServiceSummaryString(totalOnlineMatched, totalOnlineNotFound, totalOnlineError,
                totalPaperMatched, totalPaperNotFound, totalPaperError))
                .append(String.format(FORMAT_STR_LENGTH_30,TOTAL_ONLINE_STRING))
                .append(String.format(FORMAT_STR_LENGTH_8,totalOnlineMatched.intValue()))
                .append(TAB_STRING)
                .append(String.format(FORMAT_STR_LENGTH_10,totalOnlineNotFound.intValue()))
                .append(TAB_STRING)
                .append(totalOnlineError.intValue())
                .append(CR_STRING)
                .append(String.format(FORMAT_STR_LENGTH_30,TOTAL_PAPER_STRING))
                .append(String.format(FORMAT_STR_LENGTH_8,totalPaperMatched.intValue()))
                .append(TAB_STRING)
                .append(String.format(FORMAT_STR_LENGTH_10,totalPaperNotFound.intValue()))
                .append(TAB_STRING)
                .append(totalPaperError.intValue())
                .append(CR_STRING)
                .append(String.format(FORMAT_STR_LENGTH_30,TOTAL_STRING))
                .append(String.format(FORMAT_STR_LENGTH_8,totalOnlineMatched.intValue() + totalPaperMatched.intValue()))
                .append(TAB_STRING)
                .append(String.format(FORMAT_STR_LENGTH_10,totalOnlineNotFound.intValue()
                        + totalPaperNotFound.intValue()))
                .append(TAB_STRING)
                .append(totalOnlineError.intValue() + totalPaperError.intValue());

        log.info(stringBuilder.toString());
    }

    private String getSummaryString() {
        StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy", Locale.UK);
        stringBuilder.append(dateFormat.format(new Date()))
                .append(CR_STRING)
                .append(SERVICE_SUMMARY_STRING)
                .append("-----------------------------------------------------------")
                .append(CR_STRING);
        return stringBuilder.toString();
    }

    private String getServiceSummaryString(AtomicInteger totalOnlineMatched, AtomicInteger totalOnlineNotFound,
                                           AtomicInteger totalOnlineError, AtomicInteger totalPaperMatched,
                                           AtomicInteger totalPaperNotFound, AtomicInteger totalPaperError) {
        Set<String> serviceKeySet = serviceConfigProvider.getServiceNames();
        StringBuilder stringBuilder = new StringBuilder();

        serviceKeySet.forEach(service -> {
            stringBuilder.append(String.format(FORMAT_STR_LENGTH_30,service.toUpperCase(Locale.UK)
                    + ONLINE_CHANNEL_SUFFIX));
            Integer onlineMatchedCount = serviceSummaryMap.get(service + ONLINE_MATCH_SUFFIX);
            Integer onlineNotFoundCount =  serviceSummaryMap.get(service + ONLINE_NOT_FOUND_SUFFIX);
            Integer onlineErredCount = serviceSummaryMap.get(service + ONLINE_ERROR_SUFFIX);
            stringBuilder.append(countsString(onlineMatchedCount, onlineNotFoundCount, onlineErredCount))
                    .append(String.format(FORMAT_STR_LENGTH_30,service.toUpperCase(Locale.UK)
                            + PAPER_CHANNEL_SUFFIX));
            Integer paperMatchedCount = serviceSummaryMap.get(service + PAPER_MATCH_SUFFIX);
            Integer paperNotFoundCount =  serviceSummaryMap.get(service + PAPER_NOT_FOUND_SUFFIX);
            Integer paperErredCount = serviceSummaryMap.get(service + PAPER_ERROR_SUFFIX);
            stringBuilder.append(countsString(paperMatchedCount, paperNotFoundCount, paperErredCount));
            totalOnlineMatched.addAndGet(onlineMatchedCount == null ? 0 : onlineMatchedCount);
            totalOnlineNotFound.addAndGet(onlineNotFoundCount == null ? 0 : onlineNotFoundCount);
            totalOnlineError.addAndGet(onlineErredCount == null ? 0 : onlineErredCount);
            totalPaperMatched.addAndGet(paperMatchedCount == null ? 0 : paperMatchedCount);
            totalPaperNotFound.addAndGet(paperNotFoundCount == null ? 0 : paperNotFoundCount);
            totalPaperError.addAndGet(paperErredCount == null ? 0 : paperErredCount);
        });

        return stringBuilder.toString();
    }

    private String countsString(Integer matchedCount, Integer notFoundCount, Integer erredCount) {

        return String.format(FORMAT_STR_LENGTH_8, matchedCount == null ? 0 : matchedCount)
                + TAB_STRING
                + String.format(FORMAT_STR_LENGTH_10, notFoundCount == null ? 0 : notFoundCount)
                + TAB_STRING
                + (erredCount == null ? 0 : erredCount)
                + CR_STRING;
    }

}
