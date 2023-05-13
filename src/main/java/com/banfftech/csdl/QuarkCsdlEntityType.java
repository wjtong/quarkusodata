package com.banfftech.csdl;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

public class QuarkCsdlEntityType extends CsdlEntityType {
    private String quarkEntity;

    public String getQuarkEntity() {
        return quarkEntity;
    }

    public void setQuarkEntity(String quarkEntity) {
        this.quarkEntity = quarkEntity;
    }
}
