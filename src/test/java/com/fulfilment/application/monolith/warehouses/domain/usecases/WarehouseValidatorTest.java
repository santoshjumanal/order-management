package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.warehouse;
import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryLocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseValidatorTest {

    private InMemoryWarehouseStore store;
    private InMemoryLocationResolver resolver;
    private WarehouseValidator validator;

    @BeforeEach
    void setUp() {
        store = new InMemoryWarehouseStore();
        resolver = new InMemoryLocationResolver(new Location("AMSTERDAM-001", 5, 100));
        validator = new WarehouseValidator(resolver, store);
    }

    @Test
    void testValidateLocationSuccess() {
        Location loc = validator.validateLocation("AMSTERDAM-001");
        assertEquals("AMSTERDAM-001", loc.identification);
    }

    @Test
    void testValidateLocationNullRejected() {
        assertThrows(WebApplicationException.class, () -> validator.validateLocation(null));
    }

    @Test
    void testValidateLocationBlankRejected() {
        assertThrows(WebApplicationException.class, () -> validator.validateLocation("  "));
    }

    @Test
    void testValidateLocationUnknownRejected() {
        assertThrows(WebApplicationException.class, () -> validator.validateLocation("NOWHERE"));
    }

    @Test
    void testValidateBusinessUnitCodeIsFreeSuccess() {
        validator.validateBusinessUnitCodeIsFree("MWH.NEW");
    }

    @Test
    void testValidateBusinessUnitCodeNullRejected() {
        assertThrows(WebApplicationException.class, () -> validator.validateBusinessUnitCodeIsFree(null));
    }

    @Test
    void testValidateBusinessUnitCodeAlreadyExistsRejected() {
        store.create(warehouse("MWH.001", "AMSTERDAM-001", 40, 10));
        assertThrows(
                WebApplicationException.class, () -> validator.validateBusinessUnitCodeIsFree("MWH.001"));
    }

    @Test
    void testValidateCapacityAndStockSuccess() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        validator.validateCapacityAndStock(warehouse("MWH.X", "AMSTERDAM-001", 50, 20), loc);
    }

    @Test
    void testValidateCapacityNullRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        Warehouse w = warehouse("MWH.X", "AMSTERDAM-001", 50, 20);
        w.setCapacity(null);
        assertThrows(WebApplicationException.class, () -> validator.validateCapacityAndStock(w, loc));
    }

    @Test
    void testValidateCapacityZeroRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        assertThrows(
                WebApplicationException.class,
                () -> validator.validateCapacityAndStock(warehouse("MWH.X", "AMSTERDAM-001", 0, 0), loc));
    }

    @Test
    void testValidateCapacityExceedsLocationMaxRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        assertThrows(
                WebApplicationException.class,
                () ->
                        validator.validateCapacityAndStock(
                                warehouse("MWH.X", "AMSTERDAM-001", 150, 20), loc));
    }

    @Test
    void testValidateStockNullRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        Warehouse w = warehouse("MWH.X", "AMSTERDAM-001", 50, 20);
        w.setStock(null);
        assertThrows(WebApplicationException.class, () -> validator.validateCapacityAndStock(w, loc));
    }

    @Test
    void testValidateStockNegativeRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        assertThrows(
                WebApplicationException.class,
                () ->
                        validator.validateCapacityAndStock(
                                warehouse("MWH.X", "AMSTERDAM-001", 50, -5), loc));
    }

    @Test
    void testValidateStockExceedsCapacityRejected() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        assertThrows(
                WebApplicationException.class,
                () ->
                        validator.validateCapacityAndStock(
                                warehouse("MWH.X", "AMSTERDAM-001", 40, 60), loc));
    }

    @Test
    void testValidateCreationFeasibilitySuccess() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        validator.validateCreationFeasibility(loc, null);
    }

    @Test
    void testValidateCreationFeasibilityMaxReachedRejected() {
        Location loc = new Location("TILBURG-001", 1, 100);
        store.create(warehouse("MWH.EXIST", "TILBURG-001", 30, 10));
        assertThrows(
                WebApplicationException.class,
                () -> validator.validateCreationFeasibility(loc, null));
    }

    @Test
    void testValidateCreationFeasibilityExcludesReplacedSlot() {
        Location loc = new Location("TILBURG-001", 1, 100);
        store.create(warehouse("MWH.EXIST", "TILBURG-001", 30, 10));
        // Excluding the location of the warehouse being replaced should free up the slot.
        validator.validateCreationFeasibility(loc, "TILBURG-001");
    }

    @Test
    void testValidateReplacementAccommodatesPreviousSuccess() {
        Warehouse previous = warehouse("MWH.001", "AMSTERDAM-001", 40, 20);
        Warehouse next = warehouse("MWH.001", "AMSTERDAM-001", 60, 20);
        validator.validateReplacementAccommodatesPrevious(next, previous);
    }

    @Test
    void testValidateReplacementCapacityTooSmallRejected() {
        Warehouse previous = warehouse("MWH.001", "AMSTERDAM-001", 40, 30);
        Warehouse next = warehouse("MWH.001", "AMSTERDAM-001", 25, 30);
        assertThrows(
                WebApplicationException.class,
                () -> validator.validateReplacementAccommodatesPrevious(next, previous));
    }

    @Test
    void testValidateReplacementStockMismatchRejected() {
        Warehouse previous = warehouse("MWH.001", "AMSTERDAM-001", 40, 30);
        Warehouse next = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
        assertThrows(
                WebApplicationException.class,
                () -> validator.validateReplacementAccommodatesPrevious(next, previous));
    }
}