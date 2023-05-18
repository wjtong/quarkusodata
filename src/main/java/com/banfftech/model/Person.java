package com.banfftech.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Person extends GenericEntity {
    public String partyId;
    public String lastName;
    public String firstName;
}
