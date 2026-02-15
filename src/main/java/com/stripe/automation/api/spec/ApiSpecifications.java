package com.stripe.automation.api.spec;

import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.utils.CorrelationId;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public final class ApiSpecifications {
    private ApiSpecifications() {}

    public static RequestSpecification stripeRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("stripe.api.baseUrl"))
                .setContentType(ContentType.URLENC)
                .addHeader("Authorization", "Bearer " + ConfigManager.get("stripe.secretKey"))
                .addHeader("X-Correlation-Id", CorrelationId.get())
                .log(LogDetail.ALL)
                .build();
    }

    public static ResponseSpecification successSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .log(LogDetail.ALL)
                .build();
    }
}
