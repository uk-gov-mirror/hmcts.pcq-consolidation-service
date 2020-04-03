package uk.gov.hmcts.reform.pcqconsolidationservice.smoke;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmokeTestConfiguration.class})
public class SmokeTests {

    @Value("${test.instance.uri}")
    private String url;

    private static final int HTTP_OK = HttpStatus.OK.value();

    RequestSpecification requestSpec;

    @Before
    public void setUp() {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addParam("http.connection.timeout", "60000");
        builder.addParam("http.socket.timeout", "60000");
        builder.addParam("http.connection-manager.timeout", "60000");
        builder.setRelaxedHTTPSValidation();
        requestSpec = builder.build();
    }

    @Test
    public void shouldGetOkStatusFromHealthEndpointForPcqBackend() {

        ValidatableResponse response = given().spec(requestSpec)
            .when()
            .get(url + "/health")
            .then()
            .statusCode(HTTP_OK);
        assertTrue("Health endpoint should be HTTP 200 (ok)", okResponse(response));
    }

    @Test
    public void shouldGetOkStatusFromInfoEndpointForPcqBackend() {
        ValidatableResponse response = given().spec(requestSpec)
            .when()
            .get(url + "/info")
            .then()
            .statusCode(HTTP_OK)
            .body("git.commit.id", notNullValue())
            .body("git.commit.time", notNullValue());
        assertTrue("Info endpoint should be HTTP 200 (ok)", okResponse(response));
    }

    private boolean okResponse(ValidatableResponse response) {
        ExtractableResponse extractableResponse = response.extract();
        return extractableResponse.statusCode() == HTTP_OK ? Boolean.TRUE : Boolean.FALSE;
    }
}
