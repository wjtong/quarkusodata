package com.banfftech.service;

import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@ApplicationScoped
public class EntityServiceImpl implements EntityService {
    @Override
    public List<GenericEntity> findEntity(String entityName, FilterOption filterOption) {
        // use PanacheEntity
        Class<?> objectClass = null;
        List<GenericEntity> genericEntities = null;
        try {
            String packageEntityName = EdmConst.ENTITY_PACKAGE + "." + entityName;
            objectClass = Class.forName(packageEntityName);
            Method method = objectClass.getMethod("listAll");
            genericEntities = (List<GenericEntity>) method.invoke(objectClass);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return genericEntities;
    }
}
