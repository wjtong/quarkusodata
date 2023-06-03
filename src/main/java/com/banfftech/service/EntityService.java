package com.banfftech.service;

import com.banfftech.model.GenericEntity;
import com.banfftech.odata.processor.OdataExpressionVisitor;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    List<GenericEntity> findEntity(String entityName, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
    List<GenericEntity> findRelatedEntity(GenericEntity entity, String navigationName, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
}
