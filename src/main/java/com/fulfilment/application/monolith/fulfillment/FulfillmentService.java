package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service for fulfillment unit operations.
 * Delegates validation to FulfillmentValidator and handles business logic.
 */
@ApplicationScoped
public class FulfillmentService {

    @Inject FulfillmentRepository fulfillmentRepository;

    @Inject ProductRepository productRepository;

    @Inject FulfillmentValidator validator;

    @Transactional
    public FulfillmentUnit associate(Long productId, Long storeId, String warehouseBusinessUnitCode) {
        // Validate all entities exist
        validator.validateEntitiesExist(productId, storeId, warehouseBusinessUnitCode);

        // Check for idempotency: if the exact association already exists, return it
        FulfillmentUnit existing = validator.findExistingAssociation(productId, storeId, warehouseBusinessUnitCode);
        if (existing != null) {
            return existing;
        }

        // Validate all constraints
        validator.validateProductWarehouseLimit(productId, storeId);
        validator.validateStoreWarehouseLimit(storeId, warehouseBusinessUnitCode);
        validator.validateWarehouseProductLimit(productId, warehouseBusinessUnitCode);

        // Create new association
        Product product = productRepository.findById(productId);
        Store store = Store.findById(storeId);
        FulfillmentUnit unit = new FulfillmentUnit(product, store, warehouseBusinessUnitCode);
        fulfillmentRepository.persist(unit);
        return unit;
    }
}
