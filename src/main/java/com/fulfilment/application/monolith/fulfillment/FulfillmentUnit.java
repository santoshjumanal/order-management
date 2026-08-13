package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Associates a {@link com.fulfilment.application.monolith.warehouses.domain.models.Warehouse}
 * (referenced by its business unit code) as a fulfilment unit of a {@link Product} for a given
 * {@link Store}.
 */
@Entity
@Cacheable
@Table(
        name = "fulfillment_unit",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_id", "store_id", "warehouseBusinessUnitCode"}))
public class FulfillmentUnit extends PanacheEntity {

    @ManyToOne public Product product;

    @ManyToOne public Store store;

    public String warehouseBusinessUnitCode;

    public FulfillmentUnit() {}

    public FulfillmentUnit(Product product, Store store, String warehouseBusinessUnitCode) {
        this.product = product;
        this.store = store;
        this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    }
}
