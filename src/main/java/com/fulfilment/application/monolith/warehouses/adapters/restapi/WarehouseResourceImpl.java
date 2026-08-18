package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject
  private WarehouseRepository warehouseRepository;

  @Inject
  private CreateWarehouseOperation createWarehouseOperation;

  @Inject
  private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject
  private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = toDomainWarehouse(data);

    createWarehouseOperation.create(warehouse);

    return toWarehouseResponse(warehouseRepository.findByBusinessUnitCode(warehouse.getBusinessUnitCode()));
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse entity = findActiveById(id);
    return toWarehouseResponse(entity.toWarehouse());
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    DbWarehouse entity = findActiveById(id);
    archiveWarehouseOperation.archive(entity.toWarehouse());
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    var warehouse = toDomainWarehouse(data);
    warehouse.setBusinessUnitCode(businessUnitCode);

    replaceWarehouseOperation.replace(warehouse);

    return toWarehouseResponse(warehouseRepository.findByBusinessUnitCode(businessUnitCode));
  }

  private DbWarehouse findActiveById(String id) {
    Long numericId;
    try {
      numericId = Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }

    DbWarehouse entity = warehouseRepository.findById(numericId);
    if (entity == null || entity.archivedAt != null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }

    return entity;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
          Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.setBusinessUnitCode(data.getBusinessUnitCode());
    warehouse.setLocation(data.getLocation());
    warehouse.setCapacity(data.getCapacity());
    warehouse.setStock(data.getStock());
    return warehouse;
  }

  private Warehouse toWarehouseResponse(Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.getBusinessUnitCode());
    response.setLocation(warehouse.getLocation());
    response.setCapacity(warehouse.getCapacity());
    response.setStock(warehouse.getStock());
    return response;
  }
}