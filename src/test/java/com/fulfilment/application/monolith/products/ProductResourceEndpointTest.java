package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests for ProductResource endpoints.
 */
@QuarkusTest
@DisplayName("Product Endpoint Tests")
public class ProductResourceEndpointTest {

    @Inject
    ProductResource productResource;

    @Inject
    ProductRepository productRepository;

    @BeforeEach
    @Transactional
    public void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Get all products returns empty list initially")
    public void testGetAllProductsEmpty() {
        var products = productResource.get();
        assertNotNull(products);
        assertEquals(0, products.size());
    }

    @Test
    @DisplayName("Create valid product returns 201")
    @Transactional
    public void testCreateValidProduct() {
        Product product = new Product("Test Product");
        product.stock = 10;
        var response = productResource.create(product);
        assertNotNull(response);
        assertEquals(201, response.getStatus());
    }

    @Test
    @DisplayName("Get product by ID after creation")
    @Transactional
    public void testGetProductById() {
        Product product = new Product("Find Product");
        product.stock = 5;
        productRepository.persist(product);

        Product found = productResource.getSingle(product.id);
        assertNotNull(found);
        assertEquals("Find Product", found.name);
    }

    @Test
    @DisplayName("Get product by non-existent ID throws 404")
    public void testGetProductNotFound() {
        assertThrows(WebApplicationException.class, () -> {
            productResource.getSingle(99999L);
        });
    }

    @Test
    @DisplayName("Update product returns 204")
    @Transactional
    public void testUpdateProduct() {
        Product product = new Product("Original");
        product.stock = 1;
        productRepository.persist(product);

        product.name = "Updated";
        var response = productResource.update(product.id, product);
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("Update non-existent product throws 404")
    public void testUpdateProductNotFound() {
        Product product = new Product("Updated");
        assertThrows(WebApplicationException.class, () -> {
            productResource.update(99999L, product);
        });
    }

    @Test
    @DisplayName("Delete product returns 204")
    @Transactional
    public void testDeleteProduct() {
        Product product = new Product("Delete Me");
        product.stock = 1;
        productRepository.persist(product);

        var response = productResource.delete(product.id);
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("Delete non-existent product throws 404")
    public void testDeleteProductNotFound() {
        assertThrows(WebApplicationException.class, () -> {
            productResource.delete(99999L);
        });
    }

    @Test
    @DisplayName("Create product with duplicate name fails")
    @Transactional
    public void testCreateDuplicateProductName() {
        Product product1 = new Product("Duplicate");
        product1.stock = 1;
        productRepository.persist(product1);

        Product product2 = new Product("Duplicate");
        assertThrows(WebApplicationException.class, () -> {
            productResource.create(product2);
        });
    }
}
