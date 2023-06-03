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
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.*;

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
    private QuarkProcessor quarkProcessor;

    public EntityCollectionImp(QuarkProcessor quarkProcessor) {
        this.quarkProcessor = quarkProcessor;
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
            Map<String, QueryOption> queryOptions = Util.getQuernOptions(uriInfo);
            EntityCollection entityCollection = quarkProcessor.findList(edmEntityType, queryOptions);
            serializeEntityCollection(request, response, edmEntitySet, edmEntityType,
                    responseFormat, entityCollection, queryOptions);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ODataApplicationException("Error processing the request", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH, e);
        }
    }
    private void serializeEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, EdmBindingTarget edmBindingTarget, EdmEntityType edmEntityType,
                                           ContentType contentType, EntityCollection entityCollection, Map<String, QueryOption> queryOptions)
            throws ODataApplicationException {
        try {
            //响应时排除二进制数据
            for (Entity entity : entityCollection.getEntities()) {
                entity.getProperties().removeIf(property -> "Edm.Stream".equals(property.getType()));
            }
            ExpandOption expandOption = (ExpandOption) queryOptions.get("expandOption");
            SelectOption selectOption = (SelectOption) queryOptions.get("selectOption");
            CountOption countOption = (CountOption) queryOptions.get("countOption");
            String uriSetName = edmBindingTarget != null ? edmBindingTarget.getName() : edmEntityType.getName();
            String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
            ContextURL contextUrl = ContextURL.with().serviceRoot(new URI(oDataRequest.getRawBaseUri() + "/"))
                    .entitySetOrSingletonOrType(uriSetName).selectList(selectList).build();
            String id = oDataRequest.getRawBaseUri() + "/" + edmEntityType.getName();
            EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).count(countOption)
                    .contextURL(contextUrl).expand(expandOption).select(selectOption).build();
            InputStream serializedContent = odata.createSerializer(contentType)
                    .entityCollection(serviceMetadata, edmEntityType, entityCollection, opts).getContent();
            oDataResponse.setContent(serializedContent);
            oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
            oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
        } catch (URISyntaxException | SerializerException e) {
            e.printStackTrace();
            throw new ODataApplicationException(e.getMessage(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
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
