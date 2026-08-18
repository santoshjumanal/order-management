package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.stream.Collectors;

/**
 * Validator for fulfillment unit associations.
 * Encapsulates all business rule validations for product-warehouse-store associations.
 */
@ApplicationScoped
public class FulfillmentValidator {

    static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
    static final int MAX_WAREHOUSES_PER_STORE = 3;
    static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

    @Inject FulfillmentRepository fulfillmentRepository;

    @Inject WarehouseRepository warehouseRepository;

    @Inject ProductRepository productRepository;

    /**
     * Validates that all entities exist (product, store, warehouse).
     */
    public void validateEntitiesExist(Long productId, Long storeId, String warehouseBusinessUnitCode) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new WebApplicationException("Product with id " + productId + " does not exist.", 404);
        }

        Store store = Store.findById(storeId);
        if (store == null) {
            throw new WebApplicationException("Store with id " + storeId + " does not exist.", 404);
        }

        if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
            throw new WebApplicationException(
                    "Warehouse with business unit code "
                            + warehouseBusinessUnitCode
                            + " does not exist or is archived.",
                    404);
        }
    }

    /**
     * Validates Constraint 1: Each Product can be fulfilled by at most 2 different Warehouses per Store.
     */
    public void validateProductWarehouseLimit(Long productId, Long storeId) {
        long warehousesForProductInStore =
                fulfillmentRepository.listByProductAndStore(productId, storeId).stream()
                        .map(f -> f.warehouseBusinessUnitCode)
                        .distinct()
                        .count();
        if (warehousesForProductInStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
            throw new WebApplicationException(
                    "Product "
                            + productId
                            + " is already fulfilled by the maximum of "
                            + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
                            + " warehouses for store "
                            + storeId
                            + ".",
                    400);
        }
    }

    /**
     * Validates Constraint 2: Each Store can be fulfilled by at most 3 different Warehouses.
     */
    public void validateStoreWarehouseLimit(Long storeId, String warehouseBusinessUnitCode) {
        var warehousesForStore =
                fulfillmentRepository.listByStore(storeId).stream()
                        .map(f -> f.warehouseBusinessUnitCode)
                        .collect(Collectors.toSet());
        if (!warehousesForStore.contains(warehouseBusinessUnitCode)
                && warehousesForStore.size() >= MAX_WAREHOUSES_PER_STORE) {
            throw new WebApplicationException(
                    "Store "
                            + storeId
                            + " is already fulfilled by the maximum of "
                            + MAX_WAREHOUSES_PER_STORE
                            + " warehouses.",
                    400);
        }
    }

    /**
     * Validates Constraint 3: Each Warehouse can store at most 5 types of Products.
     */
    public void validateWarehouseProductLimit(Long productId, String warehouseBusinessUnitCode) {
        var productsInWarehouse =
                fulfillmentRepository.listByWarehouse(warehouseBusinessUnitCode).stream()
                        .map(f -> f.product.id)
                        .collect(Collectors.toSet());
        if (!productsInWarehouse.contains(productId)
                && productsInWarehouse.size() >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
            throw new WebApplicationException(
                    "Warehouse "
                            + warehouseBusinessUnitCode
                            + " already stores the maximum of "
                            + MAX_PRODUCT_TYPES_PER_WAREHOUSE
                            + " product types.",
                    400);
        }
    }

    /**
     * Checks if association already exists (idempotency).
     */
    public FulfillmentUnit findExistingAssociation(Long productId, Long storeId, String warehouseBusinessUnitCode) {
        return fulfillmentRepository.findByProductStoreAndWarehouse(productId, storeId, warehouseBusinessUnitCode);
    }
}
