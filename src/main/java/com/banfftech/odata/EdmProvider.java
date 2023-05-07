package com.banfftech.odata;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EdmProvider implements CsdlEdmProvider {

    private static final String NAMESPACE = "com.banfftech";
    private static final String CONTAINER_NAME = "Container";
    private static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    private static final String ENTITY_TYPE_NAME_PARTY = "Party";
    private static final String ENTITY_SET_NAME_PARTIES = "Parties";
    private static final FullQualifiedName PERSON_FQN = new FullQualifiedName(NAMESPACE, ENTITY_TYPE_NAME_PARTY);

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
}