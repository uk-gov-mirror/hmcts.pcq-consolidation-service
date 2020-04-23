package uk.gov.hmcts.reform.pcqconsolidationservice.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign.PcqBackendFeignClient;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.utils.JsonFeignResponseUtil;

@Slf4j
@Service
public class PcqBackendServiceImpl implements PcqBackendService {

    PcqBackendFeignClient pcqBackendFeignClient;

    @Value("${coRelationId:Test}")
    String coRelationHeader;

    @Autowired
    public PcqBackendServiceImpl(PcqBackendFeignClient pcqBackendFeignClient) {
        this.pcqBackendFeignClient = pcqBackendFeignClient;
    }

    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.DataflowAnomalyAnalysis"})
    public ResponseEntity getPcqWithoutCase() {
        ResponseEntity responseEntity;

        try (Response response = pcqBackendFeignClient.getPcqWithoutCase(coRelationHeader)) {
            Class clazz = response.status() == 200 || response.status() == 400 || response.status() == 500
                    ? PcqWithoutCaseResponse.class : ErrorResponse.class;
            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ex.getMessage());
        }

        return responseEntity;

    }

}
