package com.banfftech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JoinFormula;

// SupplierProduct has a many-to-one relationship with Party. The relationship is mapped by the partyId field in the SupplierProduct class.
@Entity
public class SupplierProduct extends GenericEntity {
    public String productId;
    public String partyId;
    @JoinColumn(name = "partyId", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JsonIgnore
    public Party party;

    public static String add(String partyId, String productId) {
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.partyId = partyId;
        supplierProduct.productId = productId;
        supplierProduct.persist();
        return supplierProduct.id;
    }
}
