package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface WarehouseResource {
    List<Warehouse> listAllWarehousesUnits();

    @Transactional
    Warehouse createANewWarehouseUnit(@NotNull Warehouse data);

    Warehouse getAWarehouseUnitByID(String id);

    @Transactional
    void archiveAWarehouseUnitByID(String id);

    @Transactional
    Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode, @NotNull Warehouse data);
}
