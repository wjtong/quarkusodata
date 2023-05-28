package com.banfftech.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JoinFormula;

@Entity
public class SupplierProduct extends GenericEntity {
    public String productId;
    @Column(name = "partyid")
    public String partyId;
//    @JoinTable(name = "party", joinColumns = @JoinColumn(name = "id", referencedColumnName = "partyid"))
    @ManyToOne
    @JoinFormula("partyid")
    public Party party;

    public static String add(String partyId, String productId) {
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.partyId = partyId;
        supplierProduct.productId = productId;
        supplierProduct.persist();
        return supplierProduct.id;
    }
}
