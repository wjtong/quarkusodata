package com.banfftech.model;

import com.banfftech.model.GenericEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Party extends GenericEntity {
    public String partyTypeId;
    public String externalId;
    public String description;
    public String statusId;
    public LocalDate createdDate;
    public String createdByUserLogin;
    public LocalDate lastModifiedDate;
    public String lastModifiedByUserLogin;
    public String dataSourceId;
    public boolean isUnread;
    public static String add(String id, String externalId, String description) {
        Party party = new Party();
        party.id = id;
        party.externalId = externalId;
        party.description = description;
        party.persist();
        return party.id;
    }
}
