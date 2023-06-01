package com.banfftech.odata.processor;

import com.banfftech.Util;
import com.banfftech.model.GenericEntity;
import com.banfftech.service.EntityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class QuarkProcessorImpl implements QuarkProcessor{
    @Inject
    EntityService entityService;

    @Override
    public EntityCollection findList(EdmEntityType edmEntityType, Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        String entityName = edmEntityType.getName();
        List<GenericEntity> genericEntities = entityService.findEntity(entityName, queryOptions);
        List<Entity> entities = Util.GenericToEntities(edmEntityType, genericEntities);
        EntityCollection entityCollection = new EntityCollection();
        entityCollection.getEntities().addAll(entities);
        return entityCollection;
    }
}
