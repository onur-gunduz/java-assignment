package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.warehouse.api.beans.Warehouse;
import com.warehouse.api.WarehouseResource;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseUseCase createWarehouseUseCase;
  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainModel = toDomainModel(data);

    try {
      QuarkusTransaction.requiringNew().run(() -> createWarehouseUseCase.create(domainModel));
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }

    return toWarehouseResponse(domainModel);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with code " + id + " does not exist or is archived.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with code " + id + " does not exist or is already archived.", 404);
    }

    try {
      QuarkusTransaction.requiringNew().run(() -> archiveWarehouseUseCase.archive(warehouse));
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainModel = toDomainModel(data);
    domainModel.businessUnitCode = businessUnitCode;

    try {
      QuarkusTransaction.requiringNew().run(() -> replaceWarehouseUseCase.replace(domainModel));
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }

    return toWarehouseResponse(domainModel);
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(Warehouse data) {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.businessUnitCode = data.getBusinessUnitCode();
    domain.location = data.getLocation();
    domain.capacity = data.getCapacity();
    domain.stock = data.getStock();
    return domain;
  }
}