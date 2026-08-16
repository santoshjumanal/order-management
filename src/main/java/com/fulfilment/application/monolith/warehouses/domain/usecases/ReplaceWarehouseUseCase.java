package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // The warehouse being replaced must currently exist and be active
    Warehouse previous = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());
    if (previous == null) {
      throw new WebApplicationException(
              "Warehouse with business unit code "
                      + newWarehouse.getBusinessUnitCode()
                      + " does not exist.",
              404);
    }

    // Location Validation: it must be an existing, valid location
    Location location = locationResolver.resolveByIdentifier(newWarehouse.getLocation());
    if (location == null) {
      throw new WebApplicationException(
              "Location " + newWarehouse.getLocation() + " is not a valid location.", 400);
    }

    // The warehouse being replaced is about to be archived, so it is excluded when checking
    // feasibility/capacity for the new one at that location
    List<Warehouse> otherWarehousesAtLocation =
            warehouseStore.getAll().stream()
                    .filter(w -> w.getLocation().equals(location.identification))
                    .filter(w -> !w.getBusinessUnitCode().equals(previous.getBusinessUnitCode()))
                    .toList();

    // Warehouse Creation Feasibility
    if (otherWarehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "The maximum number of warehouses for location "
                      + location.identification
                      + " has already been reached.",
              400);
    }

    if (newWarehouse.getCapacity() == null || newWarehouse.getCapacity() <= 0) {
      throw new WebApplicationException("Warehouse capacity must be a positive number.", 400);
    }
    if (newWarehouse.getStock() == null || newWarehouse.getStock() < 0) {
      throw new WebApplicationException("Warehouse stock must not be negative.", 400);
    }

    // Capacity and Stock Validation
    int alreadyUsedCapacity = otherWarehousesAtLocation.stream().mapToInt(w -> w.getCapacity()).sum();
    if (alreadyUsedCapacity + newWarehouse.getCapacity() > location.maxCapacity) {
      throw new WebApplicationException(
              "Warehouse capacity exceeds the maximum capacity available for location "
                      + location.identification,
              400);
    }
    if (newWarehouse.getStock() > newWarehouse.getCapacity()) {
      throw new WebApplicationException("Warehouse stock cannot exceed its own capacity.", 400);
    }

    // Capacity Accommodation: the new warehouse must be able to accommodate the stock
    // currently held by the warehouse being replaced
    if (newWarehouse.getCapacity() < previous.getStock()) {
      throw new WebApplicationException(
              "The new warehouse's capacity cannot accommodate the stock of the warehouse being"
                      + " replaced.",
              400);
    }

    // Stock Matching: the new warehouse's stock must match the stock of the previous warehouse
    if (!newWarehouse.getStock().equals(previous.getStock())) {
      throw new WebApplicationException(
              "The new warehouse's stock must match the stock of the warehouse being replaced.", 400);
    }

    previous.setArchivedAt(LocalDateTime.now());
    warehouseStore.update(previous);

    newWarehouse.setCreatedAt(LocalDateTime.now());
    newWarehouse.setArchivedAt(null);
    warehouseStore.create(newWarehouse);
  }
}
