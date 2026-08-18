package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Cannot create a null warehouse.");
    }
    DbWarehouse dbEntity = new DbWarehouse();
    dbEntity.businessUnitCode = warehouse.businessUnitCode;
    dbEntity.location = warehouse.location;
    dbEntity.capacity = warehouse.capacity;
    dbEntity.stock = warehouse.stock;
    dbEntity.createdAt = warehouse.createdAt;
    dbEntity.archivedAt = warehouse.archivedAt;

    this.persist(dbEntity);
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null) {
      throw new IllegalArgumentException("Cannot update warehouse without a valid Business Unit Code.");
    }

    // Find active first
    DbWarehouse dbEntity = this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

    if (dbEntity == null) {
      dbEntity = this.find("businessUnitCode = ?1", warehouse.businessUnitCode).firstResult();
    }

    if (dbEntity == null) {
      throw new IllegalArgumentException("Warehouse entity not found in database for business unit code: " + warehouse.businessUnitCode);
    }

    dbEntity.businessUnitCode = warehouse.businessUnitCode;
    dbEntity.location = warehouse.location;
    dbEntity.capacity = warehouse.capacity;
    dbEntity.stock = warehouse.stock;
    dbEntity.createdAt = warehouse.createdAt;
    dbEntity.archivedAt = warehouse.archivedAt;

    this.persist(dbEntity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null) {
      return;
    }
    this.delete("businessUnitCode", warehouse.businessUnitCode);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      return null;
    }
    DbWarehouse dbEntity = this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return dbEntity != null ? dbEntity.toWarehouse() : null;
  }
}