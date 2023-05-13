package com.banfftech.odata;

import com.banfftech.csdl.QuarkCsdlEntityType;
import com.banfftech.csdl.QuarkCsdlProperty;
import com.banfftech.edmconfig.EdmEntityType;
import com.banfftech.edmconfig.EdmServiceConfig;
import com.banfftech.model.GenericEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    @Inject
    EdmConfig edmconfig;

    public EdmService loadService(String serviceName) throws IOException, ClassNotFoundException {
        EdmService edmService = new EdmService();
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = Paths.get("config", serviceName + ".json").toString();
        EdmServiceConfig edmServiceConfig = objectMapper.readValue(new File(filePath), EdmServiceConfig.class);


        List<EdmConfig.OdataService> odataServiceList = edmconfig.services();
        for (EdmEntityType edmEntityType:edmServiceConfig.getEntityTypes()) {
            QuarkCsdlEntityType quarkCsdlEntityType = loadEntityType(edmServiceConfig, edmEntityType);
            edmService.addEntityType(quarkCsdlEntityType);
        }
        return edmService;
    }

    private QuarkCsdlEntityType loadEntityType(EdmServiceConfig edmServiceConfig, EdmEntityType edmEntityType) {
        String name = edmEntityType.getEntityName();
        String quarkEntity = name;
        String draftEntityName = null;
        String attrEntityName = null;
        String attrNumericEntityName = null;
        String attrDateEntityName = null;
        String handlerClass = null;
        String entityConditionStr = null;
        String searchOption = null;
        EntityCondition entityCondition = null;
        if (edmEntityType.getQuarkEntity() != null) {
            quarkEntity = edmEntityType.getQuarkEntity();
        }
        GenericEntity modelEntity = null;
        try {
            modelEntity = delegator.getModelReader().getModelEntity(quarkEntity);
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
        }
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
        csdlEntityType.setAbstract(isAbstract);
        csdlEntityType.setAnnotations(csdlAnnotationList);
        csdlEntityType.setTerms(terms);
        csdlEntityType.setHasRelField(hasRelField);
        csdlEntityType.setRelAliases(relAliases);
        csdlEntityType.setOpenType(openType);
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
        List<CsdlPropertyRef> propertyRefs = csdlPropertyRefs;
        if (autoProperties) {
            Field[] fields = Class.forName(quarkEntity).getDeclaredFields();
            for (Field field:fields) {
                String fieldName = field.getName();
                if (excludeProperties != null && excludeProperties.contains(fieldName)) {
                    continue;
                }
                QuarkCsdlProperty csdlProperty = generatePropertyFromField(delegator, dispatcher, field, false);
                if (csdlProperties != null) {
                    if (csdlProperties.contains(csdlProperty)) {
                        //已经xml定义了，就不要自动生成了
                        continue;
                    }
                    if (autoLabel) {
                        String label = (String) Util.getUiLabelMap(locale).get(entityName + Util.firstUpperCase(csdlProperty.getName()));
                        csdlProperty.setLabel(label);
                    }
                    csdlProperties.add(csdlProperty);
                }

            }
            Iterator<ModelField> fieldIterator = modelEntity.getFieldsIterator();
            // 获取所有的外键字段，以及关联到Enumeration表的字段
//            Set<String> fkFieldNames = Util.getEntityFk(modelEntity);
            List<String> automacticFieldNames = modelEntity.getAutomaticFieldNames(); // lastUpdatedStamp, lastUpdatedTxStamp, createdStamp, createdTxStamp
            while (fieldIterator.hasNext()) {
                ModelField field = fieldIterator.next();
                String fieldName = field.getName();
                if (automacticFieldNames.contains(fieldName)) {
                    continue;
                }
                if (excludeProperties != null && excludeProperties.contains(fieldName)) {
                    continue;
                }
                /**** fk先暂时加回来，牵扯面太广 **********************
                 //中间表的主键同时也有外键约束，这种情况应该保留主键
                 if (!pkFieldNames.contains(fieldName) && fkFieldNames.contains(fieldName)) {
                 continue;
                 }
                 **************************************************/
                OfbizCsdlProperty csdlProperty = generatePropertyFromField(delegator, dispatcher, field, false);
                if (csdlProperties != null) {
                    if (csdlProperties.contains(csdlProperty)) {
                        //已经xml定义了，就不要自动生成了
                        continue;
                    }
                    if (autoLabel) {
                        String label = (String) Util.getUiLabelMap(locale).get(entityName + Util.firstUpperCase(csdlProperty.getName()));
                        csdlProperty.setLabel(label);
                    }
                    csdlProperties.add(csdlProperty);
                }
            }
        }
        if (UtilValidate.isEmpty(propertyRefs) && UtilValidate.isNotEmpty(modelEntity)) { // EntityType的Key还没有定义
            // 先添加主键，所有odata的EntityType必须映射到一个ofbiz对象作为主对象，所以，总是可以从ofbiz主对象中获取主键字段
//            Iterator<ModelField> pksIterator = modelEntity.getPksIterator();
            List<String> ofbizAllPk = modelEntity.getPkFieldNames();
            propertyRefs = new ArrayList<>();
            for (CsdlProperty csdlProperty : csdlProperties) {
                OfbizCsdlProperty ofbizCsdlProperty = (OfbizCsdlProperty) csdlProperty;
                if (ofbizCsdlProperty.getRelAlias() != null) {
                    continue;
                }
                if (ofbizAllPk.contains(ofbizCsdlProperty.getOfbizFieldName())) {
                    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                    propertyRef.setName(ofbizCsdlProperty.getName());
                    propertyRefs.add(propertyRef);
                }
            }
//            while (pksIterator.hasNext()) {
//                ModelField field = pksIterator.next();
//                String fieldName = field.getName();
//                pkFieldNames.add(fieldName);
//                CsdlPropertyRef propertyRef = new CsdlPropertyRef();
//                propertyRef.setName(fieldName);
//                propertyRefs.add(propertyRef);
//            }
        }
        OfbizCsdlEntityType entityType = new OfbizCsdlEntityType(ofbizEntity, handlerClass, false,
                false, filterByDate, draftEntityName, attrEntityName, attrNumericEntityName, attrDateEntityName,
                hadDerivedEntity, entityCondition, entityConditionStr, labelPrefix, searchOption, groupBy, hasStream, autoLabel);
        if (UtilValidate.isNotEmpty(baseType)) {
            //有BaseType, Property里就不应该再有pk
            List<String> propertyRefNames = propertyRefs.stream().map(CsdlPropertyRef::getName).collect(Collectors.toList());
            csdlProperties.removeIf(cp -> propertyRefNames.contains(cp.getName()));
            if (baseType.indexOf('.') == -1) {
                entityType.setBaseType(new FullQualifiedName(OfbizMapOdata.NAMESPACE, baseType));
            } else {
                entityType.setBaseType(baseType);
            }
        }
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
        ModelEntity modelEntity = field.getModelEntity();
        Map<String, String> enumMap = null;
        if (autoEnum) {
            enumMap = Util.getEntityAutoEnum(modelEntity);
        }
        Type fieldType = field.getGenericType();
        String fieldTypeName = fieldType.getTypeName();
        String fieldName = field.getName();

        OfbizCsdlProperty csdlProperty = new OfbizCsdlProperty();
        csdlProperty.setName(fieldName);
        if (autoEnum && "indicator".equals(fieldType)) {
            if ("gender".equals(fieldName)) {
                csdlProperty.setType(OfbizMapOdata.NAMESPACE + ".Gender");
            } else if ("maritalStatus".equals(fieldName)) {
                csdlProperty.setType(OfbizMapOdata.NAMESPACE + ".MaritalStatus");
            } else if ("priority".equals(fieldName)) {
                csdlProperty.setType(OfbizMapOdata.NAMESPACE + ".Priority");
            } else {
                csdlProperty.setType("Edm.Boolean");
            }
        } else if (enumMap != null && enumMap.containsKey(fieldName)) { // 先只处理Product的enumeration
            csdlProperty.setType(OfbizMapOdata.NAMESPACE + "." + enumMap.get(fieldName));
        } else {
            csdlProperty.setType(OfbizMapOdata.FIELDMAP.get(fieldType).getFullQualifiedName());
        }

        if (fieldType.equals("date-time")) {
            csdlProperty.setPrecision(3);
        } else if (csdlProperty.getType().equals("Edm.String")) {
            ModelFieldType modelFieldType;
            try {
                modelFieldType = delegator.getEntityFieldType(modelEntity, fieldType);
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return null;
            }
            int maxLength = modelFieldType.stringLength();
            csdlProperty.setMaxLength(maxLength);
        } else if (csdlProperty.getType().equals("Edm.Decimal")) {
            setPropertyPrecision(delegator, csdlProperty, modelEntity, fieldType);
        }
        boolean nullable = isPropertyNullable(dispatcher, modelEntity, fieldName);
        csdlProperty.setNullable(nullable);
        csdlProperty.setOfbizFieldType(fieldType);
        csdlProperty.setOfbizFieldName(fieldName);
        return csdlProperty;
    }
}
