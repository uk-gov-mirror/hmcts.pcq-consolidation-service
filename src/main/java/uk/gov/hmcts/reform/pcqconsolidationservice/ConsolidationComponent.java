package uk.gov.hmcts.reform.pcqconsolidationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd.CcdClientApi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConsolidationComponent {

    @Autowired
    private PcqBackendService pcqBackendService;

    private final CcdClientApi ccdClientApi;

    private final Map<String, String[]> pcqIdsMap = new ConcurrentHashMap<>();

    public ConsolidationComponent(CcdClientApi ccdClientApi) {
        this.ccdClientApi = ccdClientApi;
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedLocalVariable", "PMD.ConfusingTernary"})
    public void execute() {
        try {
            /*
            // Step 1. Get the list of PCQs without Case Id.
            ResponseEntity<PcqWithoutCaseResponse> responseEntity = pcqBackendService.getPcqWithoutCase();
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                PcqWithoutCaseResponse pcqWithoutCaseResponse = responseEntity.getBody();

                if (pcqWithoutCaseResponse.getPcqId() != null) {

                    pcqIdsMap.put("PCQ_ID_FOUND", pcqWithoutCaseResponse.getPcqId());

                    for (String pcqId : pcqWithoutCaseResponse.getPcqId()) {
                        //Step 2, Invoke the Elastic Search API to get the case Ids for each Pcq.

                        //Step 3, Invoke the addCaseForPcq API to update the case id for the Pcq.
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
            */

            //Step 2, Invoke the Elastic Search API to get the case Ids for each Pcq.
            log.info("Attempting to perform an elastic search!");
            String pcqId = "23456";
            List<Long> caseReferences = ccdClientApi.getCaseRefsByPcqId(pcqId, "pcqtest");

            log.info("Found {} results, first result {}",
                    caseReferences.size(), caseReferences.size() > 0 ? caseReferences : "");

        } catch (ExternalApiException externalApiException) {
            log.error("API could not be invoked due to error message - {}", externalApiException.getErrorMessage());
            throw externalApiException;
        }
    }
}
