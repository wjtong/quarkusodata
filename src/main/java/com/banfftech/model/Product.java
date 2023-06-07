package com.banfftech.model;

import com.banfftech.model.GenericEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Entity
public class Product extends GenericEntity {
    public String productName;
    @ManyToMany
    @JoinTable(
            name = "SupplierProduct",
            joinColumns = @JoinColumn(name = "productId"),
            inverseJoinColumns = @JoinColumn(name = "partyId"))
    @JsonIgnore
    public List<Party> party;
    public static String add(String id, String productName) {
        Product product = new Product();
        product.id = id;
        product.productName = productName;
        product.persist();
        return product.id;
    }
}
