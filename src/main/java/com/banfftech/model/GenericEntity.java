package com.banfftech.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public abstract class GenericEntity extends PanacheEntityBase {

    @GeneratedValue(generator="cid")
    @GenericGenerator(name="cid", strategy= "uuid")
    @Id
    public String id;

    public Object getPk() {
        return id;
    }

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "<" + this.getPk() + ">";
    }
}
