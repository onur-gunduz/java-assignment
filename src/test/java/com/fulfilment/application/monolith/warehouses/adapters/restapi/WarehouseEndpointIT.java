package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointIT {

  @Test
  public void testSimpleListWarehouses() {
    final String path = "warehouse";

    // List all active warehouses initially seeded in the system
    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {
    final String path = "warehouse";

    // 1. List all to make sure the target warehouse MWH.001 exists
    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("MWH.012"),
                    containsString("MWH.023")
            );

    // 2. Archive the warehouse using its Business Unit Code identifier
    given()
            .when()
            .delete(path + "/MWH.001")
            .then()
            .statusCode(204);

    // 3. Fetching the specific archived item directly should yield an explicit 404 Not Found error
    given()
            .when()
            .get(path + "/MWH.001")
            .then()
            .statusCode(404);
  }

  @Test
  public void testWarehouseCreationAndReplacementLifecycle() {
    final String path = "warehouse";

    // 1. Create a brand new active warehouse matching location restrictions
    String uniqueWarehousePayload = "{\"businessUnitCode\":\"MWH.099\",\"location\":\"ZWOLLE-001\",\"capacity\":20,\"stock\":10}";

    given()
            .contentType(ContentType.JSON)
            .body(uniqueWarehousePayload)
            .when()
            .post(path)
            .then()
            .statusCode(200)
            .body(containsString("MWH.099"));

    // 2. Attempt a replacement transaction (stock level must stay identical, new capacity must be sufficient)
    String replacementPayload = "{\"location\":\"ZWOLLE-002\",\"capacity\":35,\"stock\":10}";

    given()
            .contentType(ContentType.JSON)
            .body(replacementPayload)
            .when()
            .post(path + "/MWH.099/replacement/")
            .then()
            .statusCode(200)
            .body(containsString("MWH.099"), containsString("ZWOLLE-002"));

    // 3. Attempt a replacement transaction with mismatching stock to trigger a 400 Bad Request error
    String invalidReplacementPayload = "{\"location\":\"ZWOLLE-002\",\"capacity\":35,\"stock\":99}";

    given()
            .contentType(ContentType.JSON)
            .body(invalidReplacementPayload)
            .when()
            .post(path + "/MWH.099/replacement")
            .then()
            .statusCode(400);
  }
}
