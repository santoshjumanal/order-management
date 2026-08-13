package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<FulfillmentUnit> {

    public List<FulfillmentUnit> listByStore(Long storeId) {
        return list("store.id", storeId);
    }

    public List<FulfillmentUnit> listByProductAndStore(Long productId, Long storeId) {
        return list("product.id = ?1 and store.id = ?2", productId, storeId);
    }

    public List<FulfillmentUnit> listByWarehouse(String warehouseBusinessUnitCode) {
        return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode);
    }

    public FulfillmentUnit findByProductStoreAndWarehouse(
            Long productId, Long storeId, String warehouseBusinessUnitCode) {
        return find(
                "product.id = ?1 and store.id = ?2 and warehouseBusinessUnitCode = ?3",
                productId,
                storeId,
                warehouseBusinessUnitCode)
                .firstResult();
    }
}
