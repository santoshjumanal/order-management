package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;

public class WarehouseValidator {

    private final LocationResolver locationResolver;
    private final WarehouseStore warehouseStore;

    public WarehouseValidator(LocationResolver locationResolver, WarehouseStore warehouseStore) {
        this.locationResolver = locationResolver;
        this.warehouseStore = warehouseStore;
    }

    public Location validateLocation(String locationIdentifier) {
        if (locationIdentifier == null || locationIdentifier.isBlank()) {
            throw new WebApplicationException("Location must be provided", 400);
        }
        Location location = locationResolver.resolveByIdentifier(locationIdentifier);
        if (location == null) {
            throw new WebApplicationException("Location " + locationIdentifier + " does not exist", 400);
        }
        return location;
    }

    public void validateBusinessUnitCodeIsFree(String businessUnitCode) {
        if (businessUnitCode == null || businessUnitCode.isBlank()) {
            throw new WebApplicationException("Business unit code must be provided", 400);
        }
        if (warehouseStore.findByBusinessUnitCode(businessUnitCode) != null) {
            throw new WebApplicationException(
                    "A warehouse with business unit code " + businessUnitCode + " already exists", 400);
        }
    }

    public void validateCapacityAndStock(Warehouse warehouse, Location location) {
        if (warehouse.getCapacity() == null || warehouse.getCapacity() <= 0) {
            throw new WebApplicationException("Warehouse capacity must be a positive number", 400);
        }
        if (warehouse.getCapacity() > location.maxCapacity) {
            throw new WebApplicationException(
                    "Warehouse capacity exceeds the maximum capacity allowed for location "
                            + location.identification, 400);
        }
        if (warehouse.getStock() == null || warehouse.getStock() < 0) {
            throw new WebApplicationException("Warehouse stock must not be negative", 400);
        }
        if (warehouse.getStock() > warehouse.getCapacity()) {
            throw new WebApplicationException("Warehouse stock cannot exceed its capacity", 400);
        }
    }

    public void validateCreationFeasibility(Location location, String excludeLocationOfCode) {
        long activeAtLocation =
                warehouseStore.getAll().stream()
                        .filter(w -> w.getArchivedAt() == null)
                        .filter(w -> location.identification.equals(w.getLocation()))
                        .count();
        if (location.identification.equals(excludeLocationOfCode)) {
            activeAtLocation -= 1;
        }
        if (activeAtLocation >= location.maxNumberOfWarehouses) {
            throw new WebApplicationException(
                    "Maximum number of warehouses reached for location " + location.identification, 400);
        }
    }

    public void validateReplacementAccommodatesPrevious(Warehouse newWarehouse, Warehouse previous) {
        if (newWarehouse.getCapacity() < previous.getStock()) {
            throw new WebApplicationException(
                    "New warehouse capacity cannot accommodate the stock of the warehouse being replaced", 400);
        }
        if (!newWarehouse.getStock().equals(previous.getStock())) {
            throw new WebApplicationException(
                    "New warehouse stock must match the stock of the warehouse being replaced", 400);
        }
    }
}