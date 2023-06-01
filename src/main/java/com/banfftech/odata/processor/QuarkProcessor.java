package com.banfftech.odata.processor;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.util.Map;

public interface QuarkProcessor {
    public EntityCollection findList(EdmEntityType edmEntityType, Map<String, QueryOption> queryOptions) throws ODataApplicationException;
}
