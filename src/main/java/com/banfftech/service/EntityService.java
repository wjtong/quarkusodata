package com.banfftech.service;

import com.banfftech.model.GenericEntity;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;

import java.util.List;

public class EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    public static List<GenericEntity> findEntity(String entityName, FilterOption filterOption) {
        return null;
    }
}
