package com.fulfilment.application.monolith;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.fulfillment.FulfillmentRepository;
import com.fulfilment.application.monolith.fulfillment.FulfillmentService;
import com.fulfilment.application.monolith.fulfillment.FulfillmentUnit;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentServiceDirectTest {

    @Inject
    FulfillmentService fulfillmentService;
    @Inject
    FulfillmentRepository fulfillmentRepository;
    @Inject ProductRepository productRepository;

    @Test
    @Transactional
    void testAssociateSuccess() {
        FulfillmentUnit unit = fulfillmentService.associate(2L, 2L, "MWH.001");
        assertNotNull(unit);
        assertEquals("MWH.001", unit.warehouseBusinessUnitCode);
    }

    @Test
    @Transactional
    void testAssociateIdempotent() {
        FulfillmentUnit first = fulfillmentService.associate(1L, 1L, "MWH.012");
        FulfillmentUnit second = fulfillmentService.associate(1L, 1L, "MWH.012");
        assertEquals(first.id, second.id);
    }

    @Test
    void testAssociateUnknownProductRejected() {
        assertThrows(
                WebApplicationException.class, () -> fulfillmentService.associate(99999L, 1L, "MWH.001"));
    }

    @Test
    void testAssociateUnknownStoreRejected() {
        assertThrows(
                WebApplicationException.class, () -> fulfillmentService.associate(1L, 99999L, "MWH.001"));
    }

    @Test
    void testAssociateUnknownWarehouseRejected() {
        assertThrows(
                WebApplicationException.class,
                () -> fulfillmentService.associate(1L, 1L, "DOES-NOT-EXIST"));
    }

    @Test
    @Transactional
    void testAssociateThirdWarehouseForSameProductStoreRejected() {
        fulfillmentService.associate(3L, 3L, "MWH.001");
        fulfillmentService.associate(3L, 3L, "MWH.012");
        assertThrows(
                WebApplicationException.class, () -> fulfillmentService.associate(3L, 3L, "MWH.023"));
    }

    @Test
    @Transactional
    void testRepositoryListByStore() {
        fulfillmentService.associate(1L, 2L, "MWH.001");
        var results = fulfillmentRepository.listByStore(2L);
        assertFalse(results.isEmpty());
    }

    @Test
    @Transactional
    void testRepositoryListByProductAndStore() {
        fulfillmentService.associate(2L, 1L, "MWH.012");
        var results = fulfillmentRepository.listByProductAndStore(2L, 1L);
        assertFalse(results.isEmpty());
    }

    @Test
    @Transactional
    void testRepositoryListByWarehouse() {
        fulfillmentService.associate(3L, 1L, "MWH.023");
        var results = fulfillmentRepository.listByWarehouse("MWH.023");
        assertFalse(results.isEmpty());
    }

    @Test
    @Transactional
    void testFulfillmentUnitConstructorAndFields() {
        Product product = productRepository.findById(1L);
        Store store = Store.findById(1L);
        FulfillmentUnit unit = new FulfillmentUnit(product, store, "MWH.001");
        assertEquals(product, unit.product);
        assertEquals(store, unit.store);
        assertEquals("MWH.001", unit.warehouseBusinessUnitCode);
    }
}