package com.banfftech;

import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
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
    public static Entity GenericToEntity(EdmEntityType edmEntityType, GenericEntity genericEntity) {
        try {
            Entity e1 = new Entity();
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
            return e1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Entity> GenericToEntities(EdmEntityType edmEntityType, List<GenericEntity> genericEntities) {
        List<Entity> entities = new ArrayList<>();
        for (GenericEntity genericEntity:genericEntities) {
            Entity entity = GenericToEntity(edmEntityType, genericEntity);
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
