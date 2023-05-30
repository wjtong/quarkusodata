package com.banfftech.service;

import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
import com.banfftech.odata.processor.OdataExpressionVisitor;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class EntityServiceImpl implements EntityService {
    @Override
    public List<GenericEntity> findEntity(String entityName, FilterOption filterOption) throws ODataApplicationException {
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor();
        String hql = "from " + entityName;
        String condition = null;
        try {
            if (filterOption != null) {
                condition = (String) filterOption.getExpression().accept(expressionVisitor);
                hql = hql + " where " + condition;
            }
        } catch (ExpressionVisitException e) {
            throw new ODataApplicationException(e.getMessage(),
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

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
