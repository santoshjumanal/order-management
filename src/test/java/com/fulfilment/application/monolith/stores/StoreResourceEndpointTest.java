package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests for StoreResource endpoints.
 * Tests store CRUD operations and legacy system integration.
 */
@QuarkusTest
@DisplayName("Store Resource Endpoint Tests")
public class StoreResourceEndpointTest {

    @Inject
    StoreResource storeResource;

    private Store testStore;

    @BeforeEach
    @Transactional
    public void setUp() {
        Store.deleteAll();
        testStore = new Store("Test Store");
        testStore.quantityProductsInStock = 100;
        testStore.persist();
    }

    @Test
    @DisplayName("GET /store should return all stores")
    public void testGetAllStores() {
        var stores = storeResource.get();
        assertNotNull(stores);
        assertTrue(stores.size() > 0);
    }

    @Test
    @DisplayName("GET /store/{id} should return store when exists")
    public void testGetStoreById_Found() {
        Store found = storeResource.getSingle(testStore.id);
        assertNotNull(found);
        assertEquals("Test Store", found.name);
    }

    @Test
    @DisplayName("GET /store/{id} should throw 404 when not found")
    public void testGetStoreById_NotFound() {
        assertThrows(WebApplicationException.class, () -> {
            storeResource.getSingle(99999L);
        });
    }

    @Test
    @DisplayName("POST /store should create new store")
    @Transactional
    public void testCreateStore_Valid() {
        Store newStore = new Store("New Store");
        newStore.quantityProductsInStock = 50;

        var response = storeResource.create(newStore);
        assertNotNull(response);
        assertEquals(201, response.getStatus());
    }

    @Test
    @DisplayName("POST /store should reject store with existing ID")
    public void testCreateStore_WithId_Rejected() {
        Store newStore = new Store("New Store");
        newStore.id = 1L;

        assertThrows(WebApplicationException.class, () -> {
            storeResource.create(newStore);
        });
    }

    @Test
    @DisplayName("PUT /store/{id} should update existing store")
    @Transactional
    public void testUpdateStore_Valid() {
        testStore.name = "Updated Store";
        var response = storeResource.update(testStore.id, testStore);
        assertNotNull(response);
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("PUT /store/{id} should require name")
    public void testUpdateStore_NoName_Rejected() {
        Store update = new Store();
        update.name = null;

        assertThrows(WebApplicationException.class, () -> {
            storeResource.update(testStore.id, update);
        });
    }

    @Test
    @DisplayName("PUT /store/{id} should throw 404 when not found")
    public void testUpdateStore_NotFound() {
        Store update = new Store("Updated");

        assertThrows(WebApplicationException.class, () -> {
            storeResource.update(99999L, update);
        });
    }

    @Test
    @DisplayName("PATCH /store/{id} should partially update store")
    @Transactional
    public void testPatchStore_Valid() {
        Store patch = new Store("Patched Store");
        patch.quantityProductsInStock = 200;

        var response = storeResource.patch(testStore.id, patch);
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("PATCH /store/{id} should require name")
    public void testPatchStore_NoName_Rejected() {
        Store patch = new Store();
        patch.name = null;

        assertThrows(WebApplicationException.class, () -> {
            storeResource.patch(testStore.id, patch);
        });
    }

    @Test
    @DisplayName("PATCH /store/{id} should throw 404 when not found")
    public void testPatchStore_NotFound() {
        Store patch = new Store("Patch");

        assertThrows(WebApplicationException.class, () -> {
            storeResource.patch(99999L, patch);
        });
    }

    @Test
    @DisplayName("DELETE /store/{id} should remove store")
    @Transactional
    public void testDeleteStore_Valid() {
        Long id = testStore.id;
        var response = storeResource.delete(id);
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("DELETE /store/{id} should throw 404 when not found")
    public void testDeleteStore_NotFound() {
        assertThrows(WebApplicationException.class, () -> {
            storeResource.delete(99999L);
        });
    }
}
