package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WarehouseValidator {

    private final LocationResolver locationResolver;
    private final WarehouseStore warehouseStore;

    @Inject
    public WarehouseValidator(LocationResolver locationResolver, WarehouseStore warehouseStore) {
        this.locationResolver = locationResolver;
        this.warehouseStore = warehouseStore;
    }

    public Location validateLocation(String locationIdentifier) {
        if (locationIdentifier == null || locationIdentifier.isBlank()) {
            throw new BusinessRuleValidationException("Location must be provided");
        }
        Location location = locationResolver.resolveByIdentifier(locationIdentifier);
        if (location == null) {
            throw new BusinessRuleValidationException(
                    "Location " + locationIdentifier + " does not exist");
        }
        return location;
    }

    public void validateBusinessUnitCodeIsFree(String businessUnitCode) {
        if (businessUnitCode == null || businessUnitCode.isBlank()) {
            throw new BusinessRuleValidationException("Business unit code must be provided");
        }
        if (warehouseStore.findByBusinessUnitCode(businessUnitCode) != null) {
            throw new BusinessRuleValidationException(
                    "A warehouse with business unit code " + businessUnitCode + " already exists");
        }
    }

    public void validateCapacityAndStock(Warehouse warehouse, Location location) {
        if (warehouse.getCapacity() == null || warehouse.getCapacity() <= 0) {
            throw new BusinessRuleValidationException("Warehouse capacity must be a positive number");
        }
        if (warehouse.getCapacity() > location.maxCapacity) {
            throw new BusinessRuleValidationException(
                    "Warehouse capacity exceeds the maximum capacity allowed for location "
                            + location.identification);
        }
        if (warehouse.getStock() == null || warehouse.getStock() < 0) {
            throw new BusinessRuleValidationException("Warehouse stock must not be negative");
        }
        if (warehouse.getStock() > warehouse.getCapacity()) {
            throw new BusinessRuleValidationException("Warehouse stock cannot exceed its capacity");
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
            throw new BusinessRuleValidationException(
                    "Maximum number of warehouses reached for location " + location.identification);
        }
    }

    public void validateReplacementAccommodatesPrevious(Warehouse newWarehouse, Warehouse previous) {
        if (newWarehouse.getCapacity() < previous.getStock()) {
            throw new BusinessRuleValidationException(
                    "New warehouse capacity cannot accommodate the stock of the warehouse being replaced");
        }
        if (!newWarehouse.getStock().equals(previous.getStock())) {
            throw new BusinessRuleValidationException(
                    "New warehouse stock must match the stock of the warehouse being replaced");
        }
    }
}