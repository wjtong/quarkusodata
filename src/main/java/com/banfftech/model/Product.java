package com.banfftech.model;

import com.banfftech.model.GenericEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Product extends GenericEntity {
    public String productName;
    public static String add(String id, String productName) {
        Product product = new Product();
        product.id = id;
        product.productName = productName;
        product.persist();
        return product.id;
    }
}
