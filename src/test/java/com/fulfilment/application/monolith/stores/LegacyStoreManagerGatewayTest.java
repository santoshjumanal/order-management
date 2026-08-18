package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LegacyStoreManagerGatewayTest {

    @Inject LegacyStoreManagerGateway gateway;

    @Test
    void testCreateStoreOnLegacySystemDoesNotThrow() {
        Store store = new Store("GATEWAY-TEST");
        store.quantityProductsInStock = 3;
        gateway.createStoreOnLegacySystem(store);
    }

    @Test
    void testUpdateStoreOnLegacySystemDoesNotThrow() {
        Store store = new Store("GATEWAY-TEST-2");
        store.quantityProductsInStock = 5;
        gateway.updateStoreOnLegacySystem(store);
    }
}