package com.banfftech.odata;

import com.banfftech.model.GenericEntity;
import org.apache.olingo.commons.api.data.Entity;

public class QuarkEntity extends Entity {
    private GenericEntity genericEntity;

    public GenericEntity getGenericEntity() {
        return genericEntity;
    }

    public void setGenericEntity(GenericEntity genericEntity) {
        this.genericEntity = genericEntity;
    }
}
