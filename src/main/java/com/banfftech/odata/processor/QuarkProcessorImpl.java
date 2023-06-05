package com.banfftech.odata.processor;

import com.banfftech.Util;
import com.banfftech.csdl.QuarkCsdlEntityType;
import com.banfftech.edmconfig.EdmConst;
import com.banfftech.model.GenericEntity;
import com.banfftech.odata.QuarkEntity;
import com.banfftech.service.EntityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
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
    public EntityCollection findList(EdmEntityType edmEntityType, Map<String,
            QueryOption> queryOptions) throws ODataApplicationException {
        String entityName = edmEntityType.getName();
        List<GenericEntity> genericEntities = entityService.findEntity(entityName, queryOptions);
        List<QuarkEntity> entities = Util.GenericToEntities(edmEntityType, genericEntities);
        EntityCollection entityCollection = new EntityCollection();
        entityCollection.setCount(entities.size());
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), entities, edmEntityType);
        }
        entityCollection.getEntities().addAll(entities);
        return entityCollection;
    }

    @Override
    public QuarkEntity findOne(EdmEntityType edmEntityType, List<UriParameter> keyPredicates,
                          Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        String entityName = edmEntityType.getName();
        String regexp = "\'";
        String keyText = keyPredicates.get(0).getText();
        String id = keyText.replaceAll(regexp, "");
        GenericEntity genericEntity = entityService.findEntityById(entityName, id);
        QuarkEntity entity = Util.GenericToEntity(edmEntityType, genericEntity);
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), List.of(entity), edmEntityType);
        }
        return entity;
    }

    @Override
    public QuarkEntity findRelatedOne(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
                                 Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        List<GenericEntity> genericEntities = entityService.findRelatedEntity(entity.getGenericEntity(), edmNavigationProperty.getName(), queryOptions);
        if (genericEntities != null && genericEntities.size() > 0) {
            GenericEntity genericEntity = genericEntities.get(0);
            EdmEntityType edmEntityType = (EdmEntityType) edmNavigationProperty.getType();
            QuarkEntity relatedEntity = Util.GenericToEntity(edmEntityType, genericEntity);
            if (queryOptions != null && queryOptions.get("expandOption") != null) {
                addExpandOption((ExpandOption) queryOptions.get("expandOption"), List.of(relatedEntity), edmEntityType);
            }
            return relatedEntity;
        }
        return null;
    }

    private void addExpandOption(ExpandOption expandOption, List<QuarkEntity> entities,
                                 EdmEntityType edmEntityType) throws ODataApplicationException {
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

    private void addExpandNavigation(List<QuarkEntity> entities, EdmEntityType edmEntityType, EdmNavigationProperty navigationProperty, int expandLevel) {
    }

    private void addAllExpandItem(List<QuarkEntity> entities, ExpandItem expandItem,
                                  EdmEntityType edmEntityType) throws ODataApplicationException {
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
        for (Entity entity : entities) {
            addExpandItem(entity, expandItem, edmEntityType);
        }
    }

    private void addExpandItem(Entity entity, ExpandItem expandItem, EdmEntityType edmEntityType) throws ODataApplicationException {
        EdmNavigationProperty edmNavigationProperty = null;
        LevelsExpandOption levelsExpandOption = expandItem.getLevelsOption();
        int expandLevel = 1;
        if (levelsExpandOption != null) {
            expandLevel = levelsExpandOption.getValue();
        }
        List<UriResource> expandItemPath = expandItem.getResourcePath().getUriResourceParts();
        UriResource uriResource = expandItemPath.get(0);
        if (uriResource instanceof UriResourceNavigation) {
            edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
        }
        if (edmNavigationProperty == null) {
            return;
        }
        String navPropName = edmNavigationProperty.getName();
        Map<String, QueryOption> embeddedQueryOptions = new HashMap<>();
        embeddedQueryOptions.put("orderByOption", expandItem.getOrderByOption());
        embeddedQueryOptions.put("selectOption", expandItem.getSelectOption());
        embeddedQueryOptions.put("searchOption", expandItem.getSearchOption());
        embeddedQueryOptions.put("filterOption", expandItem.getFilterOption());
        if (edmNavigationProperty.isCollection()) { // expand的对象是collection
            ExpandOption nestedExpandOption = expandItem.getExpandOption(); // expand nested in expand
            if (nestedExpandOption == null && expandLevel > 1) {
                ExpandOptionImpl expandOptionImpl = new ExpandOptionImpl();
                LevelsOptionImpl levelsOptionImpl = (LevelsOptionImpl) levelsExpandOption;
                levelsOptionImpl.setValue(expandLevel--);
                expandOptionImpl.addExpandItem(expandItem);
                nestedExpandOption = expandOptionImpl;
            }
            if (nestedExpandOption != null) {
                embeddedQueryOptions.put("expandOption", nestedExpandOption);
            }
            embeddedQueryOptions.put("expandOption", nestedExpandOption);
            expandCollection(entity, edmEntityType, edmNavigationProperty, embeddedQueryOptions);
        } else { // expand对象不是collection
            embeddedQueryOptions.put("expandOption", expandItem.getExpandOption());
            expandNonCollection(entity, edmEntityType, edmNavigationProperty, embeddedQueryOptions);
        } // end expand对象不是collection
    }

    private void expandNonCollection(Entity entity, EdmEntityType edmEntityType,
                                     EdmNavigationProperty edmNavigationProperty,
                                     Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        EntityCollection expandEntityCollection = getExpandData(entity, edmEntityType, edmNavigationProperty, queryOptions);
        if (null != expandEntityCollection && expandEntityCollection.getEntities() != null) {
            Entity expandEntity = expandEntityCollection.getEntities().get(0);
            expandEntityCollection.setCount(expandEntityCollection.getEntities().size());
            Link link = new Link();
            String navPropName = edmNavigationProperty.getName();
            link.setTitle(navPropName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);
            link.setInlineEntity(expandEntity);
            if (entity.getId() != null) {
                String linkHref = entity.getId().toString() + "/" + navPropName;
                link.setHref(linkHref);
            }
            entity.getNavigationLinks().add(link);
        }
   }

    private EntityCollection getExpandData(Entity entity, EdmEntityType edmEntityType,
                                           EdmNavigationProperty edmNavigationProperty,
                                           Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        Map<String, Object> embeddedEdmParams = new HashMap<>();
        embeddedEdmParams.put("edmEntityType", edmEntityType);
        embeddedEdmParams.put("edmNavigationProperty", edmNavigationProperty);
        return findRelatedList((QuarkEntity) entity, edmNavigationProperty, queryOptions);
    }
//    @Override
//    public EntityCollection findRelatedList(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
//                                            Map<String, QueryOption> queryOptions) throws ODataApplicationException {
//        EntityCollection entityCollection = new EntityCollection();
//        List<Entity> entities = entityCollection.getEntities();
//        List<GenericEntity> genericEntities = entityService.findRelatedEntity(entity.getGenericEntity(), edmNavigationProperty.getName(), queryOptions);
//        if (genericEntities != null && genericEntities.size() > 0) {
//            EdmEntityType edmEntityType = (EdmEntityType) edmNavigationProperty.getType();
//            List<QuarkEntity> relatedEntities = Util.GenericToEntities(edmEntityType, genericEntities);
//            if (queryOptions != null && queryOptions.get("expandOption") != null) {
//                addExpandOption((ExpandOption) queryOptions.get("expandOption"), relatedEntities, edmEntityType);
//            }
//            entities.addAll(relatedEntities);
//        }
//        return entityCollection;
//    }
    @Override
    public EntityCollection findRelatedList(QuarkEntity entity, EdmNavigationProperty edmNavigationProperty,
                                            Map<String, QueryOption> queryOptions)
            throws ODataApplicationException {
        EntityCollection entityCollection = new EntityCollection();
        List<GenericEntity> genericEntities = entityService.findRelatedEntity(entity.getGenericEntity(), edmNavigationProperty.getName(), queryOptions);
        List<QuarkEntity> entities = Util.GenericToEntities(edmNavigationProperty.getType(), genericEntities);
        //filter、orderby、page
        FilterOption filterOption = queryOptions != null? (FilterOption) queryOptions.get("filterOption"):null;
        OrderByOption orderbyOption = queryOptions != null? (OrderByOption) queryOptions.get("orderByOption"):null;
//        if (filterOption != null || orderbyOption != null) {
//            Util.filterEntityCollection(entityCollection, filterOption, orderbyOption, edmNavigationProperty.getType(),
//                    edmProvider, delegator, dispatcher, userLogin, locale, csdlNavigationProperty.isFilterByDate());
//        }
//        if (Util.isExtraOrderby(orderbyOption, navCsdlEntityType, delegator)) {
//            Util.orderbyEntityCollection(entityCollection, orderbyOption, edmNavigationProperty.getType(), edmProvider);
//        }
        entityCollection.setCount(entities.size());
        SkipOption skipOption = queryOptions != null? (SkipOption) queryOptions.get("skipOption"):null;
        TopOption topOption = queryOptions != null? (TopOption) queryOptions.get("topOption"):null;
        Util.pageEntityCollection(entityCollection, skipOption != null? skipOption.getValue():0, topOption != null? topOption.getValue(): EdmConst.MAX_ROWS);
        if (queryOptions != null && queryOptions.get("expandOption") != null) {
            addExpandOption((ExpandOption) queryOptions.get("expandOption"), entities, edmNavigationProperty.getType());
        }
        entityCollection.getEntities().addAll(entities);
        return entityCollection;
    }

    private void expandCollection(Entity entity, EdmEntityType edmEntityType,
                                  EdmNavigationProperty edmNavigationProperty,
                                  Map<String, QueryOption> queryOptions) throws ODataApplicationException {
        EntityCollection expandEntityCollection = getExpandData(entity, edmEntityType, edmNavigationProperty, queryOptions);
        String navPropName = edmNavigationProperty.getName();
        Link link = new Link();
        link.setTitle(navPropName);
        link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
        link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);
        link.setInlineEntitySet(expandEntityCollection);
        expandEntityCollection.setCount(expandEntityCollection.getEntities().size());
        if (entity.getId() != null) { // TODO:要检查一下为什么会有id为null的情况
            String linkHref = entity.getId().toString() + "/" + navPropName;
            link.setHref(linkHref);
        }
        entity.getNavigationLinks().add(link);
    }
}
