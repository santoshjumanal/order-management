package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryLocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for CreateWarehouseUseCase.
 * Tests warehouse creation with all validations.
 */
@DisplayName("Create Warehouse Use Case Tests")
public class CreateWarehouseUseCaseUnitTest {

    private CreateWarehouseUseCase useCase;
    private InMemoryWarehouseStore warehouseStore;
    private InMemoryLocationResolver locationResolver;

    @BeforeEach
    public void setUp() {
        warehouseStore = new InMemoryWarehouseStore();
        locationResolver = new InMemoryLocationResolver(
            new Location("AMSTERDAM-001", 5, 100),
            new Location("ZWOLLE-001", 1, 40)
        );
        useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
    }

    @Test
    @DisplayName("Should create warehouse with valid inputs")
    public void testCreateWarehouse_Valid() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-001");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(50);
        warehouse.setStock(25);

        assertDoesNotThrow(() -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should reject duplicate business unit code")
    public void testCreateWarehouse_DuplicateCode() {
        Warehouse wh1 = new Warehouse();
        wh1.setBusinessUnitCode("WH-001");
        wh1.setLocation("AMSTERDAM-001");
        wh1.setCapacity(50);
        wh1.setStock(0);
        useCase.create(wh1);

        Warehouse wh2 = new Warehouse();
        wh2.setBusinessUnitCode("WH-001");
        wh2.setLocation("ZWOLLE-001");
        wh2.setCapacity(30);
        wh2.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(wh2);
        });
    }

    @Test
    @DisplayName("Should reject invalid location")
    public void testCreateWarehouse_InvalidLocation() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-002");
        warehouse.setLocation("NONEXISTENT");
        warehouse.setCapacity(50);
        warehouse.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should reject null location")
    public void testCreateWarehouse_NullLocation() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-003");
        warehouse.setLocation(null);
        warehouse.setCapacity(50);
        warehouse.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should reject capacity exceeding location maximum")
    public void testCreateWarehouse_CapacityExceedsMax() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-004");
        warehouse.setLocation("AMSTERDAM-001"); // Max capacity 100
        warehouse.setCapacity(150); // Exceeds max
        warehouse.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should reject negative capacity")
    public void testCreateWarehouse_NegativeCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-005");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(-10);
        warehouse.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should reject negative stock")
    public void testCreateWarehouse_NegativeStock() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-006");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(50);
        warehouse.setStock(-5);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(warehouse);
        });
    }

    @Test
    @DisplayName("Should set creation timestamp")
    public void testCreateWarehouse_TimestampSet() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("WH-007");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(50);
        warehouse.setStock(0);

        useCase.create(warehouse);

        assertNotNull(warehouse.getCreatedAt());
        assertNull(warehouse.getArchivedAt());
    }

    @Test
    @DisplayName("Should respect warehouse limit per location")
    public void testCreateWarehouse_LocationLimit() {
        // ZWOLLE-001 has maxWarehouses = 1
        Warehouse wh1 = new Warehouse();
        wh1.setBusinessUnitCode("WH-ZW-001");
        wh1.setLocation("ZWOLLE-001");
        wh1.setCapacity(30);
        wh1.setStock(0);
        useCase.create(wh1);

        // Try to create second warehouse at same location
        Warehouse wh2 = new Warehouse();
        wh2.setBusinessUnitCode("WH-ZW-002");
        wh2.setLocation("ZWOLLE-001");
        wh2.setCapacity(30);
        wh2.setStock(0);

        assertThrows(WebApplicationException.class, () -> {
            useCase.create(wh2);
        });
    }
}
