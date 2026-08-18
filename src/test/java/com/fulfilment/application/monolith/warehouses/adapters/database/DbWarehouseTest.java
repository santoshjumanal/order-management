package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class DbWarehouseTest {

    @Test
    void testToWarehouseMapsAllFields() {
        DbWarehouse entity = new DbWarehouse();
        entity.businessUnitCode = "MWH.001";
        entity.location = "ZWOLLE-001";
        entity.capacity = 40;
        entity.stock = 10;
        entity.createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        entity.archivedAt = null;

        Warehouse warehouse = entity.toWarehouse();

        assertEquals("MWH.001", warehouse.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", warehouse.getLocation());
        assertEquals(40, warehouse.getCapacity());
        assertEquals(10, warehouse.getStock());
        assertEquals(entity.createdAt, warehouse.getCreatedAt());
        assertEquals(null, warehouse.getArchivedAt());
    }
}