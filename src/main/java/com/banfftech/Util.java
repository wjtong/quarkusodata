package com.banfftech;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Util {
    public static Entity PanacheToEntity (EdmEntityType edmEntityType, PanacheEntity panacheEntity) {
        try {
            Entity e1 = new Entity();
            String entityFqn = edmEntityType.getFullQualifiedName().getFullQualifiedNameAsString();
            e1.setType(entityFqn);
            String pkFieldName = "id";
            Object pkFieldValue = panacheEntity.id;
            Field idField = panacheEntity.getClass().getField("id");
            Field[] fields = Class.forName(entityFqn).getDeclaredFields();
            List<Field> fieldList = toListArray(fields);
            fieldList.add(idField);
            Iterator<Field> fieldIterator = fieldList.iterator();
            while (fieldIterator.hasNext()) {
                Field field = fieldIterator.next();
                String fieldName = field.getName();
                //edmConfig未定义、stamp公共字段、空值，跳过
                if (edmEntityType.getProperty(fieldName) == null) {
                    continue;
                }

                Object fieldValue = field.get(panacheEntity);
                Property theProperty = null;
                FullQualifiedName propertyFqn = edmEntityType.getProperty(fieldName).getType().getFullQualifiedName();
                if (theProperty == null) {
                    theProperty = new Property(propertyFqn.getFullQualifiedNameAsString(), fieldName, ValueType.PRIMITIVE, fieldValue);
                }
                e1.addProperty(theProperty);
            }
            return e1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Entity> PanachesToEntities(EdmEntityType edmEntityType, List<PanacheEntity> panacheEntities) {
        List<Entity> entities = new ArrayList<>();
        for (PanacheEntity panacheEntity:panacheEntities) {
            Entity entity = PanacheToEntity(edmEntityType, panacheEntity);
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
}
