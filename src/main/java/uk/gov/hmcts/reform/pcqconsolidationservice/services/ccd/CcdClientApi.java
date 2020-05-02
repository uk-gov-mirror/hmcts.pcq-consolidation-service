package uk.gov.hmcts.reform.pcqconsolidationservice.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigItem;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.ServiceConfigProvider;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class CcdClientApi {

    private final CoreCaseDataApi feignCcdApi;
    private final ServiceConfigProvider serviceConfigProvider;
    private final CcdAuthenticatorFactory authenticatorFactory;

    public static final String SEARCH_BY_PCQ_ID_QUERY_FORMAT =
            "{\"query\": { \"match_phrase\" : { \"data.pcqId\" : \"%s\" }}}";

    public CcdClientApi(
            CoreCaseDataApi feignCcdApi,
            CcdAuthenticatorFactory authenticator,
            ServiceConfigProvider serviceConfigProvider
    ) {
        this.feignCcdApi = feignCcdApi;
        this.authenticatorFactory = authenticator;
        this.serviceConfigProvider = serviceConfigProvider;
    }

    public List<Long> getCaseRefsByPcqId(String pcqId, String service) {

        ServiceConfigItem serviceConfig = serviceConfigProvider.getConfig(service);

        if (serviceConfig.getCaseTypeIds().isEmpty()) {
            log.info(
                    "Skipping case search by pcq ID ({}) for service {} because it has no case type ID configured",
                    pcqId,
                    service
            );

            return emptyList();
        } else {
            String jurisdiction = serviceConfig.getJurisdiction();
            String caseTypeIdsStr = String.join(",", serviceConfig.getCaseTypeIds());
            CcdAuthenticator authenticator = authenticatorFactory.createForJurisdiction(jurisdiction);

            log.info(
                    "Searching for pcqId {} within the service {}",
                    pcqId,
                    service
            );

            SearchResult searchResult = feignCcdApi.searchCases(
                    authenticator.getUserToken(),
                    authenticator.getServiceToken(),
                    caseTypeIdsStr,
                    format(SEARCH_BY_PCQ_ID_QUERY_FORMAT, pcqId)
            );

            return searchResult
                    .getCases()
                    .stream()
                    .map(CaseDetails::getId)
                    .collect(toList());
        }
    }
}

