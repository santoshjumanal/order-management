package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test cases for WarehouseValidator.
 * Validates all business rules for warehouse operations.
 */
@QuarkusTest
@DisplayName("Warehouse Validator Tests")
public class WarehouseValidatorTest {

    @Inject WarehouseValidator validator;
    @Inject WarehouseRepository warehouseRepository;
    @Inject LocationGateway locationGateway;

    @Test
    @DisplayName("Should throw exception when location is null")
    public void testValidateLocation_Null() {
        assertThrows(WebApplicationException.class, () -> {
            validator.validateLocation(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when location is blank")
    public void testValidateLocation_Blank() {
        assertThrows(WebApplicationException.class, () -> {
            validator.validateLocation("   ");
        });
    }

    @Test
    @DisplayName("Should throw exception when location does not exist")
    public void testValidateLocation_DoesNotExist() {
        assertThrows(WebApplicationException.class, () -> {
            validator.validateLocation("NONEXISTENT-LOCATION");
        });
    }

    @Test
    @DisplayName("Should return location when it exists")
    public void testValidateLocation_Valid() {
        Location location = validator.validateLocation("ZWOLLE-001");
        assertNotNull(location);
        assertEquals("ZWOLLE-001", location.identification);
    }

    @Test
    @DisplayName("Should throw exception when business unit code is null")
    public void testValidateBusinessUnitCodeIsFree_Null() {
        assertThrows(WebApplicationException.class, () -> {
            validator.validateBusinessUnitCodeIsFree(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when business unit code is blank")
    public void testValidateBusinessUnitCodeIsFree_Blank() {
        assertThrows(WebApplicationException.class, () -> {
            validator.validateBusinessUnitCodeIsFree("   ");
        });
    }

    @Test
    @DisplayName("Should pass when business unit code is unique")
    public void testValidateBusinessUnitCodeIsFree_Valid() {
        assertDoesNotThrow(() -> {
            validator.validateBusinessUnitCodeIsFree("UNIQUE-CODE-" + System.currentTimeMillis());
        });
    }

    @Test
    @DisplayName("Should throw exception when capacity is null")
    public void testValidateCapacityAndStock_CapacityNull() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(null);
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

        assertThrows(WebApplicationException.class, () -> {
            validator.validateCapacityAndStock(warehouse, location);
        });
    }

    @Test
    @DisplayName("Should throw exception when capacity is zero or negative")
    public void testValidateCapacityAndStock_CapacityNegative() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(0);
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

        assertThrows(WebApplicationException.class, () -> {
            validator.validateCapacityAndStock(warehouse, location);
        });
    }

    @Test
    @DisplayName("Should throw exception when capacity exceeds location maximum")
    public void testValidateCapacityAndStock_CapacityExceedsMax() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(999);
        warehouse.setStock(0);
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

        assertThrows(WebApplicationException.class, () -> {
            validator.validateCapacityAndStock(warehouse, location);
        });
    }

    @Test
    @DisplayName("Should throw exception when stock is negative")
    public void testValidateCapacityAndStock_StockNegative() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(30);
        warehouse.setStock(-5);
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

        assertThrows(WebApplicationException.class, () -> {
            validator.validateCapacityAndStock(warehouse, location);
        });
    }

    @Test
    @DisplayName("Should pass when capacity and stock are valid")
    public void testValidateCapacityAndStock_Valid() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(30);
        warehouse.setStock(20);
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

        assertDoesNotThrow(() -> {
            validator.validateCapacityAndStock(warehouse, location);
        });
    }
}
