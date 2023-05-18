package com.banfftech.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Subselect;

@Entity
@Subselect("select Party.id, Party.externalId, Person.lastName from Party join Person on Party.id = Person.partyId")
public class PartyAndPerson extends GenericEntity {
    @Id
    public String id;
    public String externalId;
    public String lastName;
}
