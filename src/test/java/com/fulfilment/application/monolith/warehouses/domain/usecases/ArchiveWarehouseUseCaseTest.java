package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @BeforeEach
    public void setUp() {
        // Manually create the mock to completely bypass the Quarkus boot lifecycle validation
        warehouseStore = mock(WarehouseStore.class);
        // Directly instantiate the use case class using its standard constructor
        archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    public void testWhenArchiveValidWarehouseShouldSetArchivedAtAndCallUpdate() {
        // given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 30;
        warehouse.stock = 15;
        warehouse.createdAt = LocalDateTime.now().minusDays(5);
        warehouse.archivedAt = null;

        // when
        archiveWarehouseUseCase.archive(warehouse);

        // then
        assertNotNull(warehouse.archivedAt);
        verify(warehouseStore).update(warehouse);
    }

    @Test
    public void testWhenArchiveNullWarehouseShouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            archiveWarehouseUseCase.archive(null);
        });
    }
}
