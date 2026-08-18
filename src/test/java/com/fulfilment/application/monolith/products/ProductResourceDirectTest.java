package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductResourceDirectTest {

    @Inject ProductResource productResource;
    @Inject ProductRepository productRepository;

    @Test
    void testGetAll() {
        List<Product> products = productResource.get();
        assertNotNull(products);
    }

    @Test
    void testGetSingleFound() {
        Product product = productResource.getSingle(2L);
        assertNotNull(product);
    }

    @Test
    void testGetSingleNotFound() {
        assertThrows(WebApplicationException.class, () -> productResource.getSingle(99999L));
    }

    @Test
    void testCreate() {
        Product product = new Product("DIRECT-PRODUCT");
        product.stock = 10;
        var response = productResource.create(product);
        assertEquals(201, response.getStatus());
    }

    @Test
    void testCreateWithIdRejected() {
        Product product = new Product("BAD");
        product.id = 999L;
        assertThrows(WebApplicationException.class, () -> productResource.create(product));
    }

    @Test
    @Transactional
    void testUpdate() {
        Product created = new Product("DIRECT-UPDATE-P");
        created.stock = 1;
        productRepository.persist(created);

        Product update = new Product("DIRECT-UPDATED-P");
        update.stock = 5;
        Product result = productResource.update(created.id, update);
        assertEquals("DIRECT-UPDATED-P", result.name);
    }

    @Test
    void testUpdateMissingNameRejected() {
        Product update = new Product();
        update.stock = 5;
        assertThrows(WebApplicationException.class, () -> productResource.update(1L, update));
    }

    @Test
    void testUpdateNotFound() {
        Product update = new Product("X");
        assertThrows(WebApplicationException.class, () -> productResource.update(99999L, update));
    }

    @Test
    @Transactional
    void testDelete() {
        Product created = new Product("DIRECT-DELETE-P");
        productRepository.persist(created);

        var response = productResource.delete(created.id);
        assertEquals(204, response.getStatus());
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(WebApplicationException.class, () -> productResource.delete(99999L));
    }
}