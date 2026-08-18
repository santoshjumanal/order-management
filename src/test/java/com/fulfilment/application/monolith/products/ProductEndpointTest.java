package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    given().when().delete(path + "/1").then().statusCode(204);

    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testGetSingleProduct() {
    given().when().get("product/2").then().statusCode(200).body(containsString("KALLAX"));
  }

  @Test
  public void testGetSingleProductNotFound() {
    given().when().get("product/9999").then().statusCode(404);
  }

  @Test
  public void testCreateProduct() {
    String payload = "{\"name\":\"NEWPRODUCT\",\"stock\":15}";
    given()
            .contentType("application/json")
            .body(payload)
            .when()
            .post("product")
            .then()
            .statusCode(201)
            .body(containsString("NEWPRODUCT"));
  }

  @Test
  public void testCreateProductWithIdRejected() {
    String payload = "{\"id\":999,\"name\":\"BAD\",\"stock\":1}";
    given()
            .contentType("application/json")
            .body(payload)
            .when()
            .post("product")
            .then()
            .statusCode(422);
  }

  @Test
  public void testUpdateProduct() {
    String createPayload = "{\"name\":\"TOUPDATE\",\"stock\":1}";
    Long id =
            given()
                    .contentType("application/json")
                    .body(createPayload)
                    .when()
                    .post("product")
                    .then()
                    .statusCode(201)
                    .extract()
                    .jsonPath()
                    .getLong("id");

    String updatePayload = "{\"name\":\"UPDATED\",\"description\":\"desc\",\"price\":9.99,\"stock\":5}";
    given()
            .contentType("application/json")
            .body(updatePayload)
            .when()
            .put("product/" + id)
            .then()
            .statusCode(200)
            .body(containsString("UPDATED"));
  }

  @Test
  public void testUpdateProductMissingNameRejected() {
    String payload = "{\"stock\":5}";
    given()
            .contentType("application/json")
            .body(payload)
            .when()
            .put("product/1")
            .then()
            .statusCode(422);
  }

  @Test
  public void testUpdateProductNotFound() {
    String payload = "{\"name\":\"X\",\"stock\":1}";
    given()
            .contentType("application/json")
            .body(payload)
            .when()
            .put("product/9999")
            .then()
            .statusCode(404);
  }

  @Test
  public void testDeleteProductNotFound() {
    given().when().delete("product/9999").then().statusCode(404);
  }
}