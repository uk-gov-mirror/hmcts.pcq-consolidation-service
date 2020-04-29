package uk.gov.hmcts.reform.pcqconsolidationservice.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.feign.PcqBackendFeignClient;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.SubmitResponse;
import uk.gov.hmcts.reform.pcqconsolidationservice.exception.ExternalApiException;
import uk.gov.hmcts.reform.pcqconsolidationservice.service.PcqBackendService;
import uk.gov.hmcts.reform.pcqconsolidationservice.utils.JsonFeignResponseUtil;

import java.io.IOException;

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
    @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.DataflowAnomalyAnalysis", "unchecked"})
    public ResponseEntity<PcqWithoutCaseResponse> getPcqWithoutCase() {
        ResponseEntity<PcqWithoutCaseResponse> responseEntity;

        try (Response response = pcqBackendFeignClient.getPcqWithoutCase(coRelationHeader)) {
            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, PcqWithoutCaseResponse.class);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ex.getMessage());
        } catch (IOException ioe) {
            throw new ExternalApiException(HttpStatus.SERVICE_UNAVAILABLE, ioe.getMessage());
        }

        return responseEntity;

    }

    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.DataflowAnomalyAnalysis", "unchecked"})
    public ResponseEntity<SubmitResponse> addCaseForPcq(String pcqId, String caseId) {
        ResponseEntity<SubmitResponse> responseEntity;

        try (Response response = pcqBackendFeignClient.addCaseForPcq(coRelationHeader, pcqId, caseId)) {
            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, SubmitResponse.class);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ex.getMessage());
        } catch (IOException ioe) {
            throw new ExternalApiException(HttpStatus.SERVICE_UNAVAILABLE, ioe.getMessage());
        }

        return responseEntity;
    }


}
