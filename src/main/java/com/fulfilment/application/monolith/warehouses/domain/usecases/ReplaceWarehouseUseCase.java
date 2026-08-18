package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Find the original warehouse that currently uses this Business Unit Code
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      throw new IllegalArgumentException("No existing active warehouse found with Business Unit Code: " + newWarehouse.businessUnitCode);
    }
    if (oldWarehouse.archivedAt != null) {
      throw new IllegalStateException("The warehouse with Business Unit Code " + newWarehouse.businessUnitCode + " is already archived.");
    }

    // Validation 1: Capacity Accommodation (New warehouse capacity must be able to hold old stock)
    if (newWarehouse.capacity == null || newWarehouse.capacity < (oldWarehouse.stock != null ? oldWarehouse.stock : 0)) {
      throw new IllegalArgumentException("The new warehouse capacity cannot accommodate the stock from the warehouse being replaced.");
    }

    // Validation 2: Stock Matching (Confirm new warehouse payload stock matches previous stock exactly)
    if (newWarehouse.stock == null || !newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new IllegalArgumentException("The stock of the new warehouse must identically match the stock of the previous warehouse.");
    }

    // Step A: Archive the old warehouse record
    oldWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(oldWarehouse);

    // Step B: Set metadata for the replacement warehouse and create it
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;

    // Use the store port to register the brand new replacement entity
    warehouseStore.create(newWarehouse);
  }
}
