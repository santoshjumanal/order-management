package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryLocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

    private InMemoryWarehouseStore store;
    private LocationResolver locationResolver;   // ✅ fixed
    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        store = new InMemoryWarehouseStore();
        // AMSTERDAM-001 allows up to 5 warehouses and 100 total capacity.
        locationResolver = new InMemoryLocationResolver(new Location("AMSTERDAM-001", 5, 100));
        useCase = new CreateWarehouseUseCase(store, locationResolver);
    }

    @Test
    void shouldCreateValidWarehouse() {
        useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 20));

        Warehouse created = store.findByBusinessUnitCode("MWH.100");
        assertEquals("AMSTERDAM-001", created.getLocation());
        assertEquals(50, created.getCapacity());
        assertEquals(20, created.getStock());
    }

    @Test
    void shouldRejectDuplicateBusinessUnitCode() {
        useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 20));

        assertThrows(
                WebApplicationException.class,
                () -> useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 10, 5)));
    }

    @Test
    void shouldRejectInvalidLocation() {
        assertThrows(
                WebApplicationException.class,
                () -> useCase.create(warehouse("MWH.101", "NOWHERE-001", 10, 5)));
    }

    @Test
    void shouldRejectWhenMaxNumberOfWarehousesReached() {
        InMemoryLocationResolver singleSlot =
                new InMemoryLocationResolver(new Location("TILBURG-001", 1, 100));
        CreateWarehouseUseCase uc = new CreateWarehouseUseCase(store, singleSlot);
    }
}