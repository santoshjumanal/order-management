package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceDirectTest {

    @Inject StoreResource storeResource;

    @Test
    void testGetAll() {
        List<Store> stores = storeResource.get();
        assertNotNull(stores);
    }

    @Test
    void testGetSingleFound() {
        Store store = storeResource.getSingle(1L);
        assertNotNull(store);
    }

    @Test
    void testGetSingleNotFound() {
        assertThrows(WebApplicationException.class, () -> storeResource.getSingle(99999L));
    }

    @Test
    void testCreate() {
        Store store = new Store("DIRECT-CREATE");
        store.quantityProductsInStock = 3;
        var response = storeResource.create(store);
        assertEquals(201, response.getStatus());
    }

    @Test
    void testCreateWithIdRejected() {
        Store store = new Store("BAD");
        store.id = 999L;
        assertThrows(WebApplicationException.class, () -> storeResource.create(store));
    }

    @Test
    @Transactional
    void testUpdate() {
        Store created = new Store("DIRECT-UPDATE");
        created.quantityProductsInStock = 1;
        created.persist();

        Store update = new Store("DIRECT-UPDATED");
        update.quantityProductsInStock = 5;
        Store result = storeResource.update(created.id, update);
        assertEquals("DIRECT-UPDATED", result.name);
    }

    @Test
    void testUpdateMissingNameRejected() {
        Store update = new Store();
        update.quantityProductsInStock = 5;
        assertThrows(WebApplicationException.class, () -> storeResource.update(1L, update));
    }

    @Test
    void testUpdateNotFound() {
        Store update = new Store("X");
        assertThrows(WebApplicationException.class, () -> storeResource.update(99999L, update));
    }

    @Test
    @Transactional
    void testPatch() {
        Store created = new Store("DIRECT-PATCH");
        created.quantityProductsInStock = 1;
        created.persist();

        Store patch = new Store("DIRECT-PATCHED");
        patch.quantityProductsInStock = 9;
        Store result = storeResource.patch(created.id, patch);
        assertEquals("DIRECT-PATCHED", result.name);
    }

    @Test
    void testPatchMissingNameRejected() {
        Store patch = new Store();
        patch.quantityProductsInStock = 5;
        assertThrows(WebApplicationException.class, () -> storeResource.patch(1L, patch));
    }

    @Test
    @Transactional
    void testDelete() {
        Store created = new Store("DIRECT-DELETE");
        created.persist();

        var response = storeResource.delete(created.id);
        assertEquals(204, response.getStatus());
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(WebApplicationException.class, () -> storeResource.delete(99999L));
    }
}