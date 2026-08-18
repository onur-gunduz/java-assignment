package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @BeforeEach
    public void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore);
    }

    @Test
    public void testWhenReplaceValidWarehouseShouldSucceed() {
        // given
        Warehouse oldWarehouse = new Warehouse();
        oldWarehouse.businessUnitCode = "MWH.001";
        oldWarehouse.location = "ZWOLLE-001";
        oldWarehouse.capacity = 30;
        oldWarehouse.stock = 15;
        oldWarehouse.archivedAt = null;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.001";
        newWarehouse.location = "ZWOLLE-002"; // Replaced in a different area of the same unit
        newWarehouse.capacity = 50; // Increased capacity
        newWarehouse.stock = 15; // Stock matches exactly

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

        // when
        replaceWarehouseUseCase.replace(newWarehouse);

        // then
        assertNotNull(oldWarehouse.archivedAt);
        assertNotNull(newWarehouse.createdAt);
        assertNull(newWarehouse.archivedAt);

        verify(warehouseStore).update(oldWarehouse);
        verify(warehouseStore).create(newWarehouse);
    }

    @Test
    public void testWhenWarehouseToReplaceDoesNotExistShouldThrowException() {
        // given
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.999";

        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals("No existing active warehouse found with Business Unit Code: MWH.999", exception.getMessage());
    }

    @Test
    public void testWhenWarehouseToReplaceIsAlreadyArchivedShouldThrowException() {
        // given
        Warehouse oldWarehouse = new Warehouse();
        oldWarehouse.businessUnitCode = "MWH.001";
        oldWarehouse.archivedAt = LocalDateTime.now();

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.001";

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals("The warehouse with Business Unit Code MWH.001 is already archived.", exception.getMessage());
    }

    @Test
    public void testWhenNewCapacityCannotAccommodateOldStockShouldThrowException() {
        // given
        Warehouse oldWarehouse = new Warehouse();
        oldWarehouse.businessUnitCode = "MWH.001";
        oldWarehouse.stock = 40; // Old warehouse has 40 units of stock
        oldWarehouse.archivedAt = null;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.001";
        newWarehouse.capacity = 30; // New warehouse capacity is too small!
        newWarehouse.stock = 40;

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals("The new warehouse capacity cannot accommodate the stock from the warehouse being replaced.", exception.getMessage());
    }

    @Test
    public void testWhenNewStockDoesNotMatchOldStockShouldThrowException() {
        // given
        Warehouse oldWarehouse = new Warehouse();
        oldWarehouse.businessUnitCode = "MWH.001";
        oldWarehouse.stock = 20;
        oldWarehouse.archivedAt = null;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.001";
        newWarehouse.capacity = 40;
        newWarehouse.stock = 25; // Mismatch! New warehouse payload says stock is 25 instead of 20

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals("The stock of the new warehouse must identically match the stock of the previous warehouse.", exception.getMessage());
    }
}
