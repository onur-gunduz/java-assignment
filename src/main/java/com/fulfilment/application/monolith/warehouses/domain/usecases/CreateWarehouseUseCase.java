package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  @Inject
  LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void create(Warehouse warehouse) {
    // 1. Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Warehouse with Business Unit Code " + warehouse.businessUnitCode + " already exists.");
    }

    // 2. Location Validation
    Location locData = locationResolver.resolveByIdentifier(warehouse.location);
    if (locData == null) {
      throw new IllegalArgumentException("Specified location " + warehouse.location + " does not exist.");
    }

    // Fetch existing active warehouses for this location to compute limits
    List<Warehouse> existingActiveInLocation = warehouseStore.getAll().stream()
            .filter(w -> warehouse.location.equalsIgnoreCase(w.location) && w.archivedAt == null)
            .toList();

    // 3. Warehouse Creation Feasibility
    if (existingActiveInLocation.size() >= locData.maxNumberOfWarehouses) {
      throw new IllegalStateException("Maximum number of warehouses (" + locData.maxNumberOfWarehouses + ") already reached for location " + warehouse.location);
    }

    // 4. Capacity and Stock Validation
    if (warehouse.stock != null && warehouse.capacity != null && warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock cannot exceed its capacity.");
    }

    int currentTotalCapacity = existingActiveInLocation.stream()
            .mapToInt(w -> w.capacity != null ? w.capacity : 0)
            .sum();

    if (currentTotalCapacity + (warehouse.capacity != null ? warehouse.capacity : 0) > locData.maxCapacity) {
      throw new IllegalArgumentException("Adding this warehouse exceeds the maximum combined capacity (" + locData.maxCapacity + ") allowed for location " + warehouse.location);
    }

    // Set auditing fields
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // Persist via store port
    warehouseStore.create(warehouse);
  }
}
