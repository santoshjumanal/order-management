package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleValidationException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse.getBusinessUnitCode() == null || newWarehouse.getBusinessUnitCode().isBlank()) {
      throw new BusinessRuleValidationException("Business unit code must be provided");
    }

    Warehouse existingActive = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());
    if (existingActive == null) {
      throw new WarehouseNotFoundException(
              "No active warehouse found with business unit code " + newWarehouse.getBusinessUnitCode());
    }

    Location location = validator.validateLocation(newWarehouse.getLocation());
    validator.validateCreationFeasibility(location, existingActive.getLocation());
    validator.validateCapacityAndStock(newWarehouse, location);
    validator.validateReplacementAccommodatesPrevious(newWarehouse, existingActive);

    existingActive.setArchivedAt(LocalDateTime.now());
    warehouseStore.update(existingActive);

    newWarehouse.setCreatedAt(LocalDateTime.now());
    newWarehouse.setArchivedAt(null);
    warehouseStore.create(newWarehouse);
  }
}