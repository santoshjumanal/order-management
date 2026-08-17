package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void create(Warehouse warehouse) {
    validator.validateBusinessUnitCodeIsFree(warehouse.getBusinessUnitCode());
    Location location = validator.validateLocation(warehouse.getLocation());
    validator.validateCreationFeasibility(location, null);
    validator.validateCapacityAndStock(warehouse, location);

    warehouse.setCreatedAt(LocalDateTime.now());
    warehouse.setArchivedAt(null);
    warehouseStore.create(warehouse);
  }
}