package com.banfftech.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.runtime.JpaOperations;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@MappedSuperclass
public abstract class GenericEntity extends PanacheEntityBase {
    public GenericEntity() {
        super();
    }

//    @GeneratedValue(generator="cid")
//    @GenericGenerator(name="cid", strategy= "uuid")
    @Id
    public String id;
    @Override
    public void persist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        super.persist();
    }

    public Object getPk() {
        return id;
    }

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "<" + this.getPk() + ">";
    }
}
