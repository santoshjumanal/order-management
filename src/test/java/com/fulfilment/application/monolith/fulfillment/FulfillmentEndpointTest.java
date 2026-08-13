package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentEndpointTest {

    private String body(long productId, long storeId, String buCode) {
        return "{\"productId\":"
                + productId
                + ",\"storeId\":"
                + storeId
                + ",\"warehouseBusinessUnitCode\":\""
                + buCode
                + "\"}";
    }

    @Test
    public void shouldAssociateWarehouseAsFulfilmentUnit() {
        given()
                .contentType("application/json")
                .body(body(2, 2, "MWH.001"))
                .when()
                .post("fulfillment")
                .then()
                .statusCode(201)
                .body("warehouseBusinessUnitCode", is("MWH.001"));
    }

    @Test
    public void shouldRejectThirdWarehouseForSameProductAndStore() {
        // product 3, store 3: first two associations are allowed ...
        given()
                .contentType("application/json")
                .body(body(3, 3, "MWH.001"))
                .when()
                .post("fulfillment")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(body(3, 3, "MWH.012"))
                .when()
                .post("fulfillment")
                .then()
                .statusCode(201);

        // ... the third distinct warehouse for the same product+store must be rejected.
        given()
                .contentType("application/json")
                .body(body(3, 3, "MWH.023"))
                .when()
                .post("fulfillment")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldRejectUnknownWarehouse() {
        given()
                .contentType("application/json")
                .body(body(1, 1, "DOES-NOT-EXIST"))
                .when()
                .post("fulfillment")
                .then()
                .statusCode(404);
    }
}
