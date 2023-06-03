package com.banfftech;

import com.banfftech.model.Party;
import com.banfftech.model.Product;
import com.banfftech.model.SupplierProduct;
import com.banfftech.model.User;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class Startup {
    @Transactional
    public void loadUsers(@Observes StartupEvent startupEvent) {
        User.deleteAll();
        User.add("admin", "admin", "admin");
        User.add("user", "user", "user");
    }

    @Transactional
    public void loadPartiesAndSuppliers(@Observes StartupEvent startupEvent) {
        Party.deleteAll();
        SupplierProduct.deleteAll();
        String partyId1 = Party.add("10000", "ext10000", "10000 description");
        String productId1 = Product.add("PRD10000", "10000 product name");
        SupplierProduct.add(partyId1, productId1);
        String partyId2 = Party.add("10100", "ext10100", "10100 description");
        String productId2 = Product.add("PRD10100", "10100 product name");
        SupplierProduct.add(partyId2, productId2);
        String partyId3 = Party.add("10200", "ext10200", "10200 description");
        String productId3 = Product.add("PRD10200", "10200 product name");
        SupplierProduct.add(partyId3, productId3);
        SupplierProduct.add(partyId3, productId1);
        SupplierProduct.add(partyId3, productId2);
    }
}
