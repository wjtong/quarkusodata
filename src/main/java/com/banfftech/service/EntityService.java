package com.banfftech.service;

import com.banfftech.model.GenericEntity;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;

import java.util.List;

public interface EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    List<GenericEntity> findEntity(String entityName, FilterOption filterOption);
}
