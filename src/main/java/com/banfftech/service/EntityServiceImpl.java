package com.banfftech.service;

import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
import com.banfftech.odata.processor.OdataExpressionVisitor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class EntityServiceImpl implements EntityService {
    @Inject
    private Session session;

    @Override
    public List<GenericEntity> findEntity(String entityName, FilterOption filterOption) throws ODataApplicationException {
        String packageEntityName = EdmConst.ENTITY_PACKAGE + "." + entityName;
        OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor();
        String hql = "from " + entityName;
        String condition = null;
        try {
            if (filterOption != null) {
                condition = (String) filterOption.getExpression().accept(expressionVisitor);
                hql = hql + " where " + condition;
            }
            Query query = session.createQuery(hql, Class.forName(packageEntityName));
            List<GenericEntity> result = query.list();
            return result;
        } catch (ExpressionVisitException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new ODataApplicationException(e.getMessage(),
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

//        // use PanacheEntity
//        Class<?> objectClass = null;
//        List<GenericEntity> genericEntities = null;
//        try {
//            String packageEntityName = EdmConst.ENTITY_PACKAGE + "." + entityName;
//            objectClass = Class.forName(packageEntityName);
//            Method method = objectClass.getMethod("listAll");
//            genericEntities = (List<GenericEntity>) method.invoke(objectClass);
//        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//        return genericEntities;
    }
}
