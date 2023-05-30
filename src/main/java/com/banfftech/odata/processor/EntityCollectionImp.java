package com.banfftech.odata.processor;

import com.banfftech.Util;
import com.banfftech.csdl.QuarkCsdlEntityType;
import com.banfftech.model.GenericEntity;
import com.banfftech.odata.EdmProvider;
import com.banfftech.service.EntityService;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntityCollectionImp implements org.apache.olingo.server.api.processor.EntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private EntityService entityService;

    public EntityCollectionImp(EntityService entityService) {
        this.entityService = entityService;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException {
        try {
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();
            String entityName = edmEntityType.getName();
            Map<String, QueryOption> queryOptions = Util.getQuernOptions(uriInfo);
            FilterOption filterOption = (FilterOption) queryOptions.get("filterOption");
            List<GenericEntity> genericEntities = entityService.findEntity(entityName, filterOption);
            List<Entity> persons = Util.GenericToEntities(edmEntityType, genericEntities);

            // Create an EntityCollection and set the sample data
            EntityCollection entityCollection = new EntityCollection();
            entityCollection.getEntities().addAll(persons);

            // Serialize the EntityCollection
            ODataSerializer serializer = odata.createSerializer(responseFormat);
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with().contextURL(contextUrl).build();
            SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, options);
            InputStream serializedContent = serializerResult.getContent();

            // Set response data
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ODataApplicationException("Error processing the request", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH, e);
        }
    }

    private URI createId(String entitySetName, String id) {
        try {
            return new URI(entitySetName + "(" + id + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
}
