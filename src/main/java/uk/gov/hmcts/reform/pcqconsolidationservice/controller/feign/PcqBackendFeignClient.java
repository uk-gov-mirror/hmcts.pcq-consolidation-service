package uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign;

import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.pcqconsolidationservice.config.FeignInterceptorConfiguration;

@FeignClient(name = "PcqBackendFeignClient", url = "${pcqBackendUrl}", configuration =
        FeignInterceptorConfiguration.class)
public interface PcqBackendFeignClient {

    @GetMapping("/pcq/backend/consolidation/pcqRecordWithoutCase")
    @RequestLine("GET /pcq/backend/consolidation/pcqRecordWithoutCase")
    Response getPcqWithoutCase(@RequestHeader("X-Correlation-Id") String token);

    @PutMapping("/pcq/backend/consolidation/addCaseForPCQ/{pcqId}")
    Response addCaseForPcq(@RequestHeader("X-Correlation-Id") String token, @PathVariable("pcqId") String pcqId,
                           @RequestParam("caseId") String caseId);

}
