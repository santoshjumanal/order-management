package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseResourceEndpointTest {

    @Test
    void testListAllWarehouses() {
        given().when().get("warehouse").then().statusCode(200).body(containsString("MWH.001"));
    }

    @Test
    void testGetWarehouseById() {
        given().when().get("warehouse/1").then().statusCode(200).body(containsString("ZWOLLE-001"));
    }

    @Test
    void testGetWarehouseByIdNotFound() {
        given().when().get("warehouse/9999").then().statusCode(404);
    }

    @Test
    void testCreateWarehouse() {
        String payload =
                "{\"businessUnitCode\":\"MWH.900\",\"location\":\"EINDHOVEN-001\",\"capacity\":20,\"stock\":5}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("warehouse")
                .then()
                .statusCode(200)
                .body(containsString("MWH.900"));
    }

    @Test
    void testCreateWarehouseDuplicateBuCodeRejected() {
        String payload =
                "{\"businessUnitCode\":\"MWH.001\",\"location\":\"EINDHOVEN-001\",\"capacity\":20,\"stock\":5}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("warehouse")
                .then()
                .statusCode(400);
    }

    @Test
    void testArchiveWarehouse() {
        String payload =
                "{\"businessUnitCode\":\"MWH.901\",\"location\":\"HELMOND-001\",\"capacity\":10,\"stock\":2}";
        given().contentType("application/json").body(payload).when().post("warehouse").then().statusCode(200);

        Long dbId =
                given()
                        .when()
                        .get("warehouse")
                        .then()
                        .extract()
                        .jsonPath()
                        .getLong("find { it.businessUnitCode == 'MWH.901' }.id");

        given().when().delete("warehouse/" + dbId).then().statusCode(204);
    }

    @Test
    void testArchiveWarehouseNotFound() {
        given().when().delete("warehouse/9999").then().statusCode(404);
    }

    @Test
    void testReplaceWarehouse() {
        String createPayload =
                "{\"businessUnitCode\":\"MWH.902\",\"location\":\"TILBURG-001\",\"capacity\":10,\"stock\":3}";
        given()
                .contentType("application/json")
                .body(createPayload)
                .when()
                .post("warehouse")
                .then()
                .statusCode(400); // TILBURG-001 already at max (1) from seed data — expected rejection

        String replacePayload =
                "{\"businessUnitCode\":\"MWH.023\",\"location\":\"TILBURG-001\",\"capacity\":40,\"stock\":27}";
        given()
                .contentType("application/json")
                .body(replacePayload)
                .when()
                .post("warehouse/MWH.023/replacement")
                .then()
                .statusCode(200)
                .body(containsString("MWH.023"));
    }

    @Test
    void testReplaceWarehouseNotFound() {
        String payload =
                "{\"businessUnitCode\":\"MWH.NOPE\",\"location\":\"TILBURG-001\",\"capacity\":10,\"stock\":1}";
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("warehouse/MWH.NOPE/replacement")
                .then()
                .statusCode(404);
    }
}