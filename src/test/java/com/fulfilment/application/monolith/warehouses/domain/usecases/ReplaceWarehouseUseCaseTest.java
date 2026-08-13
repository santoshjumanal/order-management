package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.warehouse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryLocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

    private InMemoryWarehouseStore store;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        store = new InMemoryWarehouseStore();
        InMemoryLocationResolver locationResolver =
                new InMemoryLocationResolver(new Location("AMSTERDAM-001", 5, 100));
        useCase = new ReplaceWarehouseUseCase(store, locationResolver);
    }

    private void seedExisting(String buCode, int capacity, int stock) {
        Warehouse existing = warehouse(buCode, "AMSTERDAM-001", capacity, stock);
        store.create(existing);
    }

    @Test
    void shouldReplaceWarehouseAndArchivePrevious() {
        seedExisting("MWH.001", 40, 20);

        useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 60, 20));

        Warehouse active = store.findByBusinessUnitCode("MWH.001");
        assertNotNull(active);
        assertEquals(60, active.capacity);
        // The previous instance should now be archived.
        long archived = store.warehouses.stream().filter(w -> w.archivedAt != null).count();
        assertEquals(1, archived);
    }

    @Test
    void shouldRejectWhenWarehouseToReplaceDoesNotExist() {
        assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(warehouse("MWH.999", "AMSTERDAM-001", 40, 10)));
    }

    @Test
    void shouldRejectWhenNewCapacityCannotAccommodatePreviousStock() {
        seedExisting("MWH.001", 40, 30);

        assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 25, 30)));
    }

    @Test
    void shouldRejectWhenStockDoesNotMatchPrevious() {
        seedExisting("MWH.001", 40, 30);

        assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 50, 10)));
    }

    @Test
    void shouldRejectInvalidLocation() {
        seedExisting("MWH.001", 40, 20);

        assertThrows(
                WebApplicationException.class,
                () -> useCase.replace(warehouse("MWH.001", "NOWHERE-001", 40, 20)));
    }
}
