package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

    @Test
    public void testListStores() {
        given().when().get("store").then().statusCode(200).body(containsString("TONSTAD"));
    }

    @Test
    public void testGetSingleStoreNotFound() {
        given().when().get("store/9999").then().statusCode(404);
    }

    @Test
    public void testCreateStore() {
        String payload = "{\"name\":\"NEWSTORE\",\"quantityProductsInStock\":7}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("store")
                .then()
                .statusCode(201)
                .body(containsString("NEWSTORE"));
    }

    @Test
    public void testCreateStoreWithIdRejected() {
        String payload = "{\"id\":999,\"name\":\"BAD\",\"quantityProductsInStock\":1}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("store")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpdateStore() {
        String createPayload = "{\"name\":\"TOUPDATE\",\"quantityProductsInStock\":1}";
        Long id =
                given()
                        .contentType("application/json")
                        .body(createPayload)
                        .when()
                        .post("store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        String updatePayload = "{\"name\":\"UPDATED\",\"quantityProductsInStock\":5}";
        given()
                .contentType("application/json")
                .body(updatePayload)
                .when()
                .put("store/" + id)
                .then()
                .statusCode(200)
                .body(containsString("UPDATED"));
    }

    @Test
    public void testUpdateStoreMissingNameRejected() {
        String payload = "{\"quantityProductsInStock\":5}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("store/1")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpdateStoreNotFound() {
        String payload = "{\"name\":\"X\",\"quantityProductsInStock\":1}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("store/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testPatchStore() {
        String createPayload = "{\"name\":\"TOPATCH\",\"quantityProductsInStock\":1}";
        Long id =
                given()
                        .contentType("application/json")
                        .body(createPayload)
                        .when()
                        .post("store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        String patchPayload = "{\"name\":\"PATCHED\",\"quantityProductsInStock\":9}";
        given()
                .contentType("application/json")
                .body(patchPayload)
                .when()
                .patch("store/" + id)
                .then()
                .statusCode(200)
                .body(containsString("PATCHED"));
    }

    @Test
    public void testDeleteStore() {
        String createPayload = "{\"name\":\"TODELETE\",\"quantityProductsInStock\":1}";
        Long id =
                given()
                        .contentType("application/json")
                        .body(createPayload)
                        .when()
                        .post("store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        given().when().delete("store/" + id).then().statusCode(204);

        given().when().get("store").then().statusCode(200).body(not(containsString("TODELETE")));
    }

    @Test
    public void testDeleteStoreNotFound() {
        given().when().delete("store/9999").then().statusCode(404);
    }
}