package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    // only currently active (non-archived) warehouses are considered part of the active listing
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    // A business unit code can be shared by an archived (historical) row and, at most, one
    // active row at a time. Since update() is only ever called on a warehouse that was itself
    // fetched while still active, we match on the currently-active row to avoid touching a
    // stale, already-archived record with the same code.
    DbWarehouse entity =
            this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
                    .firstResult();

    if (entity == null) {
      throw new WebApplicationException(
              "Warehouse with business unit code " + warehouse.businessUnitCode + " does not exist.",
              404);
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;

    this.persist(entity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse entity =
            this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
                    .firstResult();

    if (entity != null) {
      this.delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }
}
