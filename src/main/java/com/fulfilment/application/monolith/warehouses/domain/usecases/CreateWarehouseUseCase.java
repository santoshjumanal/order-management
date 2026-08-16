package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse.getBusinessUnitCode() == null || warehouse.getBusinessUnitCode().isBlank()) {
      throw new WebApplicationException("Warehouse business unit code must be informed.", 400);
    }

    // Business Unit Code Verification: it must not already exist
    if (warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode()) != null) {
      throw new WebApplicationException(
              "A warehouse with business unit code "
                      + warehouse.getBusinessUnitCode()
                      + " already exists.",
              400);
    }

    // Location Validation: it must be an existing, valid location
    Location location = locationResolver.resolveByIdentifier(warehouse.getLocation());
    if (location == null) {
      throw new WebApplicationException(
              "Location " + warehouse.getLocation() + " is not a valid location.", 400);
    }

    List<Warehouse> warehousesAtLocation = warehousesActiveAt(location.identification);

    // Warehouse Creation Feasibility: the maximum number of warehouses for the location
    // must not have been reached yet
    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "The maximum number of warehouses for location "
                      + location.identification
                      + " has already been reached.",
              400);
    }

    validateCapacityAndStock(warehouse, location, warehousesAtLocation);

    warehouse.setCreatedAt(LocalDateTime.now());
    warehouse.setArchivedAt(null);

    warehouseStore.create(warehouse);
  }

  private List<Warehouse> warehousesActiveAt(String locationIdentifier) {
    return warehouseStore.getAll().stream()
            .filter(w -> w.getLocation().equals(locationIdentifier))
            .toList();
  }

  private void validateCapacityAndStock(
          Warehouse warehouse, Location location, List<Warehouse> warehousesAtLocation) {
    if (warehouse.getCapacity() == null || warehouse.getCapacity() <= 0) {
      throw new WebApplicationException("Warehouse capacity must be a positive number.", 400);
    }

    if (warehouse.getStock() == null || warehouse.getStock() < 0) {
      throw new WebApplicationException("Warehouse stock must not be negative.", 400);
    }

    // Capacity and Stock Validation: it must not exceed the maximum capacity of the location
    int alreadyUsedCapacity = warehousesAtLocation.stream().mapToInt(w -> w.getCapacity()).sum();
    if (alreadyUsedCapacity + warehouse.getCapacity() > location.maxCapacity) {
      throw new WebApplicationException(
              "Warehouse capacity exceeds the maximum capacity available for location "
                      + location.identification,
              400);
    }

    // ... and it must be able to handle the stock informed
    if (warehouse.getStock() > warehouse.getCapacity()) {
      throw new WebApplicationException("Warehouse stock cannot exceed its own capacity.", 400);
    }
  }
}
