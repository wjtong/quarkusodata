package com.banfftech;

import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
import com.banfftech.odata.QuarkEntity;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.*;

import java.lang.reflect.Field;
import java.util.*;

public class Util {
    public static QuarkEntity GenericToEntity(EdmEntityType edmEntityType, GenericEntity genericEntity) {
        try {
            QuarkEntity e1 = new QuarkEntity();
            String entityFqn = edmEntityType.getFullQualifiedName().getFullQualifiedNameAsString();
            e1.setType(entityFqn);
            String classPackage = EdmConst.ENTITY_PACKAGE + "." + edmEntityType.getName();
            Field idField = genericEntity.getClass().getField("id");
            Field[] fields = Class.forName(classPackage).getDeclaredFields();
//            Field[] fields = Class.forName(classPackage).getFields();
            List<Field> fieldList = toListArray(fields);
            fieldList.add(idField);
            Iterator<Field> fieldIterator = fieldList.iterator();
            while (fieldIterator.hasNext()) {
                Field field = fieldIterator.next();
                field.setAccessible(true);
                String fieldName = field.getName();
                //edmConfig未定义、stamp公共字段、空值，跳过
                if (edmEntityType.getProperty(fieldName) == null) {
                    continue;
                }

                Object fieldValue = field.get(genericEntity);
                Property theProperty = null;
                FullQualifiedName propertyFqn = edmEntityType.getProperty(fieldName).getType().getFullQualifiedName();
                if (theProperty == null) {
                    theProperty = new Property(propertyFqn.getFullQualifiedNameAsString(), fieldName, ValueType.PRIMITIVE, fieldValue);
                }
                e1.addProperty(theProperty);
            }
            e1.setGenericEntity(genericEntity);
            return e1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<QuarkEntity> GenericToEntities(EdmEntityType edmEntityType, List<GenericEntity> genericEntities) {
        List<QuarkEntity> entities = new ArrayList<>();
        for (GenericEntity genericEntity:genericEntities) {
            QuarkEntity entity = GenericToEntity(edmEntityType, genericEntity);
            entities.add(entity);
        }
        return entities;
    }

    public static <T> List<T> toListArray(T[] data) {
        if (data == null) {
            return null;
        }
        List<T> list = new LinkedList<>();
        for (T value: data) {
            list.add(value);
        }
        return list;
    }
    public static String getQueryString(String filter, String expand) {
        String queryString = null;
        if (filter != null) {
            queryString = "$filter=" + filter;
        }
        if (expand != null) {
            queryString = queryString != null ? queryString + "&$expand=" + expand : "$expand=" + expand;
        }
        return queryString;
    }

    public static Map<String, QueryOption> getQuernOptions(UriInfo uriInfo) {
        Map<String, QueryOption> queryOptions = new HashMap<>();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        CountOption countOption = uriInfo.getCountOption();
        SearchOption searchOption = uriInfo.getSearchOption();
        FilterOption filterOption = uriInfo.getFilterOption();
        TopOption topOption = uriInfo.getTopOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        ApplyOption applyOption = uriInfo.getApplyOption();
        if (selectOption != null) {
            queryOptions.put("selectOption", selectOption);
        }
        if (expandOption != null) {
            queryOptions.put("expandOption", expandOption);
        }
        if (skipOption != null) {
            queryOptions.put("skipOption", skipOption);
        }
        if (countOption != null) {
            queryOptions.put("countOption", countOption);
        }
        if (searchOption != null) {
            queryOptions.put("searchOption", searchOption);
        }
        if (filterOption != null) {
            queryOptions.put("filterOption", filterOption);
        }
        if (topOption != null) {
            queryOptions.put("topOption", topOption);
        }
        if (orderByOption != null) {
            queryOptions.put("orderByOption", orderByOption);
        }
        if (applyOption != null) {
            queryOptions.put("applyOption", applyOption);
        }
        return queryOptions;
    }
}
