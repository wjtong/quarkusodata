package com.banfftech.odata;

import com.banfftech.csdl.QuarkCsdlEntitySet;
import com.banfftech.csdl.QuarkCsdlEntityType;
import com.banfftech.csdl.QuarkCsdlProperty;
import com.banfftech.edmconfig.EdmConst;
import com.banfftech.edmconfig.EdmEntityType;
import com.banfftech.edmconfig.EdmServiceConfig;
import com.banfftech.model.GenericEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class EdmConfigLoader {
    public EdmConfigLoader() {
    }

    public EdmService loadService(String serviceName) throws IOException, ClassNotFoundException {
        EdmService edmService = new EdmService();
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = Paths.get("config", serviceName + ".json").toString();
        EdmServiceConfig edmServiceConfig = objectMapper.readValue(new File(filePath), EdmServiceConfig.class);

        for (EdmEntityType edmEntityType:edmServiceConfig.getEntityTypes()) {
            QuarkCsdlEntityType quarkCsdlEntityType = loadEntityType(edmServiceConfig, edmEntityType);
            QuarkCsdlEntitySet quarkCsdlEntitySet = loadEntitySet(edmServiceConfig, quarkCsdlEntityType);
            edmService.addEntityType(quarkCsdlEntityType);
            edmService.addEntitySet(quarkCsdlEntitySet);
        }
        edmService.setNamespace(edmServiceConfig.getNameSpace());
        return edmService;
    }

    private QuarkCsdlEntitySet loadEntitySet(EdmServiceConfig edmServiceConfig, QuarkCsdlEntityType quarkCsdlEntityType) {
        QuarkCsdlEntitySet quarkCsdlEntitySet = new QuarkCsdlEntitySet();
        quarkCsdlEntitySet.setName(quarkCsdlEntityType.getName());
        quarkCsdlEntitySet.setType(new FullQualifiedName(edmServiceConfig.getNameSpace(), quarkCsdlEntityType.getName()));
        return quarkCsdlEntitySet;
    }

    private QuarkCsdlEntityType loadEntityType(EdmServiceConfig edmServiceConfig, EdmEntityType edmEntityType) throws ClassNotFoundException {
        String name = edmEntityType.getEntityName();
        String quarkEntity = EdmConst.ENTITY_PACKAGE + "." + name;
        String draftEntityName = null;
        String attrEntityName = null;
        String attrNumericEntityName = null;
        String attrDateEntityName = null;
        String handlerClass = null;
        String entityConditionStr = null;
        String searchOption = null;
        if (edmEntityType.getQuarkEntity() != null) {
            quarkEntity = edmEntityType.getQuarkEntity();
        }
        GenericEntity modelEntity = null;
        List<CsdlProperty> csdlProperties = new ArrayList<>();
        List<CsdlNavigationProperty> csdlNavigationProperties = null;
        List<CsdlPropertyRef> csdlPropertyRefs = null;
        boolean filterByDate = false;
        String labelPrefix = name;
        boolean hasDerivedEntity = false;
        boolean autoProperties = edmEntityType.isAutoProperties();; // 缺省从ofbiz的entity定义中获取全部字段
        //是否自动生成所有Property的Label
        List<String> excludeProperties = new ArrayList<>();
        FullQualifiedName fullQualifiedName = new FullQualifiedName(edmServiceConfig.getNameSpace(), name);
        boolean hasRelField = false;
        QuarkCsdlEntityType csdlEntityType = createEntityType(fullQualifiedName, quarkEntity,
                draftEntityName, attrEntityName, attrNumericEntityName, attrDateEntityName, handlerClass, autoProperties,
                csdlProperties, csdlNavigationProperties, csdlPropertyRefs, filterByDate, hasDerivedEntity,
                excludeProperties, entityConditionStr, labelPrefix, searchOption);
        return csdlEntityType;
    }

    private static QuarkCsdlEntityType createEntityType(FullQualifiedName entityTypeFqn, String quarkEntity,
                                                        String draftEntityName, String attrEntityName, String attrNumericEntityName, String attrDateEntityName,
                                                        String handlerClass, boolean autoProperties,
                                                        List<CsdlProperty> csdlProperties,
                                                        List<CsdlNavigationProperty> csdlNavigationProperties,
                                                        List<CsdlPropertyRef> csdlPropertyRefs, boolean filterByDate,
                                                        boolean hadDerivedEntity, List<String> excludeProperties,
                                                        String entityConditionStr,
                                                        String labelPrefix, String searchOption) throws ClassNotFoundException {
        String entityName = entityTypeFqn.getName(); // Such as Invoice
        List<CsdlPropertyRef> propertyRefs = csdlPropertyRefs = new ArrayList<>();
        if (autoProperties) {
            Field[] fields = Class.forName(quarkEntity).getDeclaredFields();
            for (Field field:fields) {
                String fieldName = field.getName();
                if (excludeProperties != null && excludeProperties.contains(fieldName)) {
                    continue;
                }
                QuarkCsdlProperty csdlProperty = generatePropertyFromField(field, false);
                if (csdlProperty != null) {
                    if (csdlProperties.contains(csdlProperty)) {
                        //已经xml定义了，就不要自动生成了
                        continue;
                    }
                    csdlProperties.add(csdlProperty);
                }
            }
            // 添加主键
            QuarkCsdlProperty csdlProperty = new QuarkCsdlProperty();
            csdlProperty.setName("id");
            csdlProperty.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            csdlProperties.add(csdlProperty);
        }
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("id");
        propertyRefs.add(propertyRef);
        QuarkCsdlEntityType entityType = new QuarkCsdlEntityType();
        entityType.setQuarkEntity(quarkEntity);
        entityType.setName(entityName);
        entityType.setProperties(csdlProperties);
        if (csdlNavigationProperties != null) {
            entityType.setNavigationProperties(csdlNavigationProperties);
        }
        if (propertyRefs != null) {
            entityType.setKey(propertyRefs);
        }
        return entityType;
    }
    private static QuarkCsdlProperty generatePropertyFromField(Field field, boolean autoEnum) {
        if (field == null) {
            return null;
        }
        Type fieldType = field.getGenericType();
        String fieldTypeName = fieldType.getTypeName();
        String fieldName = field.getName();

        QuarkCsdlProperty csdlProperty = new QuarkCsdlProperty();
        csdlProperty.setName(fieldName);
        if (fieldTypeName.equals("java.lang.String")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        } else if (fieldTypeName.equals("java.time.LocalDate")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        } else if (fieldTypeName.equals("boolean")) {
            csdlProperty.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
        } else {
            return null;
        }
        return csdlProperty;
    }
}
