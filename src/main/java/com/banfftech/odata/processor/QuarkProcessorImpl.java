package com.banfftech.odata.processor;

import com.banfftech.Util;
import com.banfftech.csdl.QuarkCsdlEntityType;
import com.banfftech.model.GenericEntity;
import com.banfftech.service.EntityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.LevelsOptionImpl;

import java.util.*;
import java.util.stream.Collectors;

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
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), entities, edmEntityType);
        }
        return entityCollection;
    }

    private void addExpandOption(ExpandOption expandOption, List<Entity> entities, EdmEntityType edmEntityType) {
        if (expandOption == null) {
            return;
        }
        List<ExpandItem> expandItems = expandOption.getExpandItems();
        ExpandItem firstExpandItem = expandItems.get(0);
        if (firstExpandItem.isStar()) {
            LevelsExpandOption levelsExpandOption = firstExpandItem.getLevelsOption();
            int expandLevel = 1;
            if (levelsExpandOption != null) {
                expandLevel = levelsExpandOption.getValue();
            }
            List<String> navigationNames = edmEntityType.getNavigationPropertyNames();
            for (String navigationName : navigationNames) {
                EdmNavigationProperty navigationProperty = edmEntityType.getNavigationProperty(navigationName);
                addExpandNavigation(entities, edmEntityType, navigationProperty, expandLevel);
            }
        } else {
            for (ExpandItem expandItem : expandItems) {
                addAllExpandItem(entities, expandItem, edmEntityType);
            }
        }
    }

    private void addExpandNavigation(List<Entity> entities, EdmEntityType edmEntityType, EdmNavigationProperty navigationProperty, int expandLevel) {
    }

    private void addAllExpandItem(List<Entity> entities, ExpandItem expandItem, EdmEntityType edmEntityType) {
        EdmNavigationProperty edmNavigationProperty = null;
        LevelsExpandOption levelsExpandOption = expandItem.getLevelsOption();
        int expandLevel = 1;
        if (levelsExpandOption != null) {
            expandLevel = levelsExpandOption.getValue();
        }
        UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
        if (uriResource instanceof UriResourceNavigation) {
            edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
        }
        if (edmNavigationProperty == null) {
            return;
        }
        Map<String, Object> embeddedEdmParams = new HashMap<>();
        embeddedEdmParams.put("edmEntityType", edmEntityType);
        embeddedEdmParams.put("edmNavigationProperty", edmNavigationProperty);
        Map<String, QueryOption> embeddedQueryOptions = new HashMap<>();
        embeddedQueryOptions.put("expandOption", expandItem.getExpandOption());
        embeddedQueryOptions.put("filterOption", expandItem.getFilterOption());
//        UtilMisc.toMap("expandOption", expandItem.getExpandOption(),
//        "orderByOption", expandItem.getOrderByOption(), "selectOption", expandItem.getSelectOption(), "filterOption", expandItem.getFilterOption(),
//        "skipOption", expandItem.getSkipOption(), "topOption", expandItem.getTopOption());
        if (edmNavigationProperty.isCollection()) {
            ExpandOption nestedExpandOption = expandItem.getExpandOption();
            if (nestedExpandOption == null && expandLevel > 1) {
                ExpandOptionImpl expandOptionImpl = new ExpandOptionImpl();
                LevelsOptionImpl levelsOptionImpl = (LevelsOptionImpl) levelsExpandOption;
                levelsOptionImpl.setValue(expandLevel--);
                expandOptionImpl.addExpandItem(expandItem);
                nestedExpandOption = expandOptionImpl;
                embeddedQueryOptions.put("expandOption", nestedExpandOption);
            }
        }
//        OdataReader reader = new OdataReader(getOdataContext(), embeddedQueryOptions, embeddedEdmParams);
        this.addDefaultExpandLink(entities, edmNavigationProperty, embeddedQueryOptions);
    }

    private void addDefaultExpandLink(List<Entity> entities, EdmNavigationProperty edmNavigationProperty, Map<String, QueryOption> queryOptions) {
//        FilterOption filterOption = (FilterOption) queryOptions.get("filterOption");
//        //filter的条件
//        if (filterOption != null) {
//            OdataExpressionVisitor expressionVisitor = new OdataExpressionVisitor();
//            try {
//                condition = (EntityCondition) filterOption.getExpression().accept(expressionVisitor);
//            } catch (ExpressionVisitException | ODataApplicationException e) {
//                throw new OfbizODataException(e.getMessage());
//            }
//        }
//        EdmBindingTarget navBindingTarget = null;
//        EdmBindingTarget edmBindingTarget = (EdmBindingTarget) edmParams.get("edmBindingTarget");
//        if (edmBindingTarget != null) {
//            navBindingTarget = Util.getNavigationTargetEntitySet(edmBindingTarget, edmNavigationProperty);
//        }
//        EdmEntityType edmNavigationPropertyType = edmNavigationProperty.getType();
//        OfbizCsdlEntityType csdlEntityType = (OfbizCsdlEntityType) edmProvider.getEntityType(edmEntityType.getFullQualifiedName());
//        OfbizCsdlNavigationProperty csdlNavigationProperty = (OfbizCsdlNavigationProperty) csdlEntityType.getNavigationProperty(edmNavigationProperty.getName());
//        OfbizCsdlEntityType navCsdlEntityType = (OfbizCsdlEntityType) edmProvider.getEntityType(csdlNavigationProperty.getTypeFQN());
//        List<String> orderbyList = Util.convertOrderbyToField(navCsdlEntityType, orderbyOption);
//        List<GenericValue> genericValueList = entityList.stream().map(e -> ((OdataOfbizEntity) e).getGenericValue()).collect(Collectors.toList());
//        //find
//        List<GenericValue> relatedGenericList = getAllDataFromRelations(genericValueList, csdlNavigationProperty, condition, orderbyList);
//        if (UtilValidate.isEmpty(relatedGenericList)) {
//            return;
//        }
//        List<Entity> relatedEntityList = new ArrayList<>();
//        for (GenericValue genericValue : relatedGenericList) {
//            Entity resultToEntity = findResultToEntity(navBindingTarget, edmNavigationPropertyType, genericValue);
//            if (navCsdlEntityType.hasStream()) {
//                resultToEntity.getProperties().removeIf(property -> "Edm.Stream".equals(property.getType()));
//            }
//            relatedEntityList.add(resultToEntity);
//        }
//        //获取relation关联字段
//        ModelEntity modelEntity = delegator.getModelEntity(csdlEntityType.getOfbizEntity());
//        EntityTypeRelAlias relAlias = csdlNavigationProperty.getRelAlias();
//        List<String> relations = relAlias.getRelations();
//        List<ModelKeyMap> relKeyMaps = modelEntity.getRelation(relations.get(0)).getKeyMaps();
//        List<String> fieldNames = new ArrayList<>();
//        List<String> relFieldNames = new ArrayList<>();
//        for (ModelKeyMap relKeyMap : relKeyMaps) {
//            fieldNames.add(relKeyMap.getFieldName());
//            String relFieldName = relKeyMap.getRelFieldName();
//            //多段的relations查询时是添加了前缀的,所以当取值的时候也要加前缀
//            if (relations.size() > 1) {
//                relFieldName = relations.get(0) + Util.firstUpperCase(relFieldName);
//            }
//            relFieldNames.add(relFieldName);
//        }
//        Map<GenericValue, Entity> expandDataMap = new LinkedHashMap<>();
//        for (int i = 0; i < relatedEntityList.size(); i++) {
//            expandDataMap.put(relatedGenericList.get(i), relatedEntityList.get(i));
//        }
//        //处理下一层expand
//        recursionExpand(entityList, expandDataMap, navBindingTarget, edmNavigationProperty, relAlias, fieldNames, relFieldNames);
//        //将查询出来的数据根据主外键进行匹配
//        if (edmNavigationProperty.isCollection()) {
//            Map<String, Entity> mainEntityMap = new HashMap<>();
//            for (Entity entity : entities) {
//                OdataOfbizEntity mainEntity = (OdataOfbizEntity) entity;
//                String fkString = getFieldShortValue(fieldNames, mainEntity.getGenericValue());
//                mainEntityMap.put(fkString, entity);
//            }
//            for (Map.Entry<GenericValue, Entity> entry : expandDataMap.entrySet()) {
//                String fkString = getFieldShortValue(relFieldNames, entry.getKey());
//                addEntityToLink(mainEntityMap.get(fkString), edmNavigationProperty, entry.getValue());
//            }
////            //分页InlineEntitySet
////            if (queryOptions.get("skipOption") != null || queryOptions.get("topOption") != null) {
////                for (Entity entity : entityList) {
////                    Link navigationLink = entity.getNavigationLink(edmNavigationProperty.getName());
////                    EntityCollection entityCollection = navigationLink.getInlineEntitySet();
////                    Util.pageEntityCollection(entityCollection, skipValue, topValue);
////                }
////            }
//        } else {
//            Map<String, Entity> subEntityMap = new HashMap<>();
//            for (Map.Entry<GenericValue, Entity> entry : expandDataMap.entrySet()) {
//                String fkString = getFieldShortValue(relFieldNames, entry.getKey());
//                subEntityMap.put(fkString, entry.getValue());
//            }
//            for (Entity entity : entities) {
//                OdataOfbizEntity mainOfbizEn = (OdataOfbizEntity) entity;
//                String fkString = getFieldShortValue(fieldNames, mainOfbizEn.getGenericValue());
//                addEntityToLink(entity, edmNavigationProperty, subEntityMap.get(fkString));
//            }
//        }
    }

}
