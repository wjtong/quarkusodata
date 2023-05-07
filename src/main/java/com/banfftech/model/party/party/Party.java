package com.banfftech.model.party.party;

import com.banfftech.model.GenericEntity;
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
}
