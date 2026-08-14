package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.warehouse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseTestDoubles.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

    private InMemoryWarehouseStore store;
    private ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        store = new InMemoryWarehouseStore();
        useCase = new ArchiveWarehouseUseCase(store);
    }

    @Test
    void shouldArchiveExistingWarehouse() {
        Warehouse existing = warehouse("MWH.001", "AMSTERDAM-001", 40, 20);
        store.create(existing);

        useCase.archive(warehouse("MWH.001", "AMSTERDAM-001", 40, 20));

        assertNotNull(existing.getArchivedAt());
        // Once archived it is no longer returned as active.
        assertNull(store.findByBusinessUnitCode("MWH.001"));
    }

    @Test
    void shouldRejectArchivingNonExistingWarehouse() {
        assertThrows(
                WebApplicationException.class,
                () -> useCase.archive(warehouse("MWH.999", "AMSTERDAM-001", 40, 20)));
    }
}
