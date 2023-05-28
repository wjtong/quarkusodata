package com.banfftech;

import com.banfftech.model.Party;
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
        String partyId = Party.add("10000", "ext10000", "10000 description");
        SupplierProduct.add(partyId, "PRD10000");
        partyId = Party.add("10100", "ext10100", "10100 description");
        SupplierProduct.add(partyId, "PRD10100");
        partyId = Party.add("10200", "ext10200", "10200 description");
        SupplierProduct.add(partyId, "PRD10200");
    }
}
