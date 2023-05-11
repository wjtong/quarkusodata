package com.banfftech.odata;

import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

import java.io.IOException;
import java.util.*;

public class EdmProvider implements CsdlEdmProvider {

    private static final String NAMESPACE = "com.banfftech";
    private static final String CONTAINER_NAME = "Container";
    private static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    private static final String ENTITY_TYPE_NAME_PARTY = "Party";
    private static final String ENTITY_SET_NAME_PARTIES = "Parties";
    private static final FullQualifiedName PERSON_FQN = new FullQualifiedName(NAMESPACE, ENTITY_TYPE_NAME_PARTY);
    private static CsdlSchema csdlSchema;
    @Inject
    EdmConfigLoader edmConfigLoader;

    public EdmProvider(String serviceName) {
        try {
            edmConfigLoader.loadService(serviceName);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        if (entityTypeName.equals(PERSON_FQN)) {
            CsdlProperty partyId = new CsdlProperty()
                    .setName("id")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                    .setNullable(false);
            CsdlProperty partyName = new CsdlProperty()
                    .setName("partyName")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty statusId = new CsdlProperty()
                    .setName("statusId")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("id");

            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(ENTITY_TYPE_NAME_PARTY)
                    .setProperties(Arrays.asList(partyId, partyName, statusId))
                    .setKey(Collections.singletonList(propertyRef));

            return entityType;
        }

        return null;
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public CsdlTerm getTerm(FullQualifiedName fullQualifiedName) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        if (entityContainer.equals(CONTAINER_FQN)) {
            if (entitySetName.equals(ENTITY_SET_NAME_PARTIES)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ENTITY_SET_NAME_PARTIES)
                        .setType(PERSON_FQN);

                return entitySet;
            }
        }

        return null;
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        List<CsdlEntitySet> entitySets = Collections.singletonList(getEntitySet(CONTAINER_FQN, ENTITY_SET_NAME_PARTIES));

        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME)
                .setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName fullQualifiedName, String s) throws ODataException {
        return null;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER_FQN)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER_FQN);

            return entityContainerInfo;
        }

        return null;
    }

    @Override
    public List<CsdlAliasInfo> getAliasInfos() throws ODataException {
        return null;
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        // Create schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // Add EntityType to schema
        List<CsdlEntityType> entityTypes = Collections.singletonList(getEntityType(PERSON_FQN));
        schema.setEntityTypes(entityTypes);

        // Add EntityContainer to schema
        schema.setEntityContainer(getEntityContainer());

        // Return schema in a list
        return Collections.singletonList(schema);
    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) {
        // We don't have any EnumTypes in this example
        return null;
    }

    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) {
        // We don't have any TypeDefinitions in this example
        return null;
    }

    private CsdlSchema createSchema(String namespace, EdmService edmService) throws ODataException {
        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(namespace);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.addAll(edmService.getEntityTypes());
        // schema.setEntityTypes(edmConfig.getEntityTypes(edmWebConfig));
        // add complex types
        List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
        complexTypes.addAll(edmService.getComplexTypes());
        // add enum types
        List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
        enumTypes.addAll(edmService.getEnumTypes());
        // add functions
        List<CsdlFunction> functions = new ArrayList<CsdlFunction>();
        functions.addAll(edmService.getFunctions());
        // add actions
        List<CsdlAction> actions = new ArrayList<CsdlAction>();
        actions.addAll(edmService.getActions());
        // add EntityContainer
//		schema.setEntityContainer(getEntityContainer());
        schema.setEntityContainer(this.createEntityContainer(edmService));
        // add Annotations
        List<CsdlAnnotations> annotationses = new ArrayList<CsdlAnnotations>();
        annotationses.addAll(edmService.getAnnotationses());
        // add Terms
        List<CsdlTerm> terms = new ArrayList<CsdlTerm>();
        terms.addAll(edmService.getTerms());

        schema.setEntityTypes(entityTypes);
        schema.setComplexTypes(complexTypes);
        schema.setEnumTypes(enumTypes);
        schema.setFunctions(functions);
        schema.setActions(actions);
        schema.setAnnotationsGroup(annotationses);
        schema.setTerms(terms);

        return schema;
    }

    private CsdlEntityContainer createEntityContainer(EdmService edmService) throws ODataException {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName("BfContainer");

        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.addAll(edmService.getEntitySets());

        List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();
        functionImports.addAll(edmService.getFunctionImports());

        List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
        actionImports.addAll(edmService.getActionImports());

        List<CsdlSingleton> singletons = new ArrayList<CsdlSingleton>();
        singletons.addAll(edmService.getSingletons());

        entityContainer.setEntitySets(entitySets);
        entityContainer.setFunctionImports(functionImports);
        entityContainer.setActionImports(actionImports);
        entityContainer.setSingletons(singletons);

        return entityContainer;
    }
}