package com.banfftech.service;

import com.banfftech.model.GenericEntity;
import com.banfftech.odata.processor.OdataExpressionVisitor;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import java.util.List;
import java.util.Locale;

public class EntityService {
    // Based on the input parameter FilterOption which is OData query option, return the find result of the query
    public static List<GenericEntity> findEntity(String entityName, FilterOption filterOption)
            throws ODataApplicationException {
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
        return null;
    }
}
