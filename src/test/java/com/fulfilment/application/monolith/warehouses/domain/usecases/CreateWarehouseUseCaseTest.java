package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private LocationResolver locationResolver;
    private CreateWarehouseUseCase createWarehouseUseCase;

    @BeforeEach
    public void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        locationResolver = mock(LocationResolver.class);
        createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore);

        // Inject mock port resolver directly to avoid reflection dependencies
        createWarehouseUseCase.locationResolver = locationResolver;
    }

    @Test
    public void testWhenCreateValidWarehouseShouldSucceed() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        Location location = new Location("ZWOLLE-001", 3, 100);

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        // when
        createWarehouseUseCase.create(warehouse);

        // then
        assertNotNull(warehouse.createdAt);
        verify(warehouseStore).create(warehouse);
    }

    @Test
    public void testWhenBusinessUnitCodeAlreadyExistsShouldThrowException() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(new Warehouse());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });
        assertEquals("Warehouse with Business Unit Code MWH.001 already exists.", exception.getMessage());
    }

    @Test
    public void testWhenLocationDoesNotExistShouldThrowException() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "INVALID-LOC";

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });
        assertEquals("Specified location INVALID-LOC does not exist.", exception.getMessage());
    }

    @Test
    public void testWhenMaxNumberOfWarehousesReachedShouldThrowException() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.002";
        warehouse.location = "ZWOLLE-001";

        Location location = new Location("ZWOLLE-001", 1, 100);
        Warehouse existingWarehouse = new Warehouse();
        existingWarehouse.location = "ZWOLLE-001";
        existingWarehouse.archivedAt = null;

        when(warehouseStore.findByBusinessUnitCode("MWH.002")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
        when(warehouseStore.getAll()).thenReturn(List.of(existingWarehouse));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });
        assertEquals("Maximum number of warehouses (1) already reached for location ZWOLLE-001", exception.getMessage());
    }

    @Test
    public void testWhenStockExceedsCapacityShouldThrowException() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 20;
        warehouse.stock = 25; // Exceeds capacity

        Location location = new Location("ZWOLLE-001", 3, 100);

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });
        assertEquals("Warehouse stock cannot exceed its capacity.", exception.getMessage());
    }

    @Test
    public void testWhenTotalCapacityExceededShouldThrowException() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.002";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        Location location = new Location("ZWOLLE-001", 3, 80);
        Warehouse existingWarehouse = new Warehouse();
        existingWarehouse.location = "ZWOLLE-001";
        existingWarehouse.capacity = 40; // 40 + 50 = 90 (Exceeds maxCapacity of 80)
        existingWarehouse.archivedAt = null;

        when(warehouseStore.findByBusinessUnitCode("MWH.002")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
        when(warehouseStore.getAll()).thenReturn(List.of(existingWarehouse));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });
        assertEquals("Adding this warehouse exceeds the maximum combined capacity (80) allowed for location ZWOLLE-001", exception.getMessage());
    }
}
