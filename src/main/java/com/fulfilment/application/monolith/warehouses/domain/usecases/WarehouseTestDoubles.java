package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;

/** Lightweight in-memory test doubles used by the warehouse use case unit tests. */
final class WarehouseTestDoubles {

    private WarehouseTestDoubles() {}

    static final class InMemoryWarehouseStore implements WarehouseStore {
        final List<Warehouse> warehouses = new ArrayList<>();

        @Override
        public List<Warehouse> getAll() {
            return warehouses.stream().filter(w -> w.getArchivedAt() == null).toList();
        }

        @Override
        public void create(Warehouse warehouse) {
            warehouses.add(warehouse);
        }

        @Override
        public void update(Warehouse warehouse) {
            // The tests mutate the same instance, so nothing else is required here.
        }

        @Override
        public void remove(Warehouse warehouse) {
            warehouses.removeIf(w -> w.getBusinessUnitCode().equals(warehouse.getBusinessUnitCode()));
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            return warehouses.stream()
                    .filter(w -> w.getArchivedAt() == null)
                    .filter(w -> w.getBusinessUnitCode().equals(buCode))
                    .findFirst()
                    .orElse(null);
        }
    }

    static final class InMemoryLocationResolver implements LocationResolver {
        final List<Location> locations = new ArrayList<>();

        InMemoryLocationResolver(Location... locs) {
            for (Location l : locs) {
                locations.add(l);
            }
        }

        @Override
        public Location resolveByIdentifier(String identifier) {
            return locations.stream()
                    .filter(l -> l.identification.equals(identifier))
                    .findFirst()
                    .orElse(null);
        }
    }

    static Warehouse warehouse(String buCode, String location, int capacity, int stock) {
        Warehouse w = new Warehouse();
        w.setBusinessUnitCode(buCode);
        w.setLocation(location);
        w.setCapacity(capacity);
        w.setStock(stock);
        return w;
    }
}
