package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.stream.Collectors;

@ApplicationScoped
public class FulfillmentService {

    static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
    static final int MAX_WAREHOUSES_PER_STORE = 3;
    static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

    @Inject FulfillmentRepository fulfillmentRepository;

    @Inject WarehouseRepository warehouseRepository;

    @Inject
    ProductRepository productRepository;

    @Transactional
    public FulfillmentUnit associate(Long productId, Long storeId, String warehouseBusinessUnitCode) {
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

        // Idempotency: if the exact association already exists, do not create a duplicate.
        FulfillmentUnit existing =
                fulfillmentRepository.findByProductStoreAndWarehouse(
                        productId, storeId, warehouseBusinessUnitCode);
        if (existing != null) {
            return existing;
        }

        // Constraint 1: each Product can be fulfilled by at most 2 different Warehouses per Store.
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

        // Constraint 2: each Store can be fulfilled by at most 3 different Warehouses.
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

        // Constraint 3: each Warehouse can store at most 5 types of Products.
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

        FulfillmentUnit unit = new FulfillmentUnit(product, store, warehouseBusinessUnitCode);
        fulfillmentRepository.persist(unit);
        return unit;
    }
}
