package com.banfftech.odata;

import com.banfftech.odata.processor.EntityCollectionImp;
import com.banfftech.odata.processor.QuarkProcessor;
import com.banfftech.service.EntityService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

@Path("/odata.svc")
public class ODataResource {
    private static final Logger LOGGER = Logger.getLogger(ODataResource.class.getName());
    @Inject
    EdmConfigLoader edmConfigLoader;
    @Inject
    QuarkProcessor quarkProcessor;

    private static String serviceName = "CustRequestManage"; // should be from request path
    @GET
    @Path("$metadata")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadata() {
//        LOGGER.info(edmConfig.toString());
        try {
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            edmProvider.loadService();
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataSerializer serializer = odata.createSerializer(ContentType.APPLICATION_XML);
            SerializerResult serializerResult = serializer.metadataDocument(serviceMetadata);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(serializerResult.getContent(), outputStream);

            return Response.ok(outputStream.toByteArray()).type(ContentType.APPLICATION_XML.toContentTypeString()).build();
        } catch (ODataException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while generating the OData metadata document: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/")
    public Response serviceRoot() {
        return Response.ok().build();
    }

    @GET
    @Path("/{odataPath:.+}")
    public Response processODataRequest(@PathParam("odataPath") String odataPath, @QueryParam("$filter") String filter) {
        ODataRequest request = new ODataRequest();
        String baseUri = "http://localhost:8080/odata.svc";
        String queryString = filter != null ? "$filter=" + filter : null;
        request.setRawBaseUri(baseUri);
        request.setRawODataPath(odataPath);
        request.setRawServiceResolutionUri("/");
        request.setMethod(HttpMethod.GET);
        request.setRawRequestUri(baseUri + "/" + odataPath + (queryString != null ? "?" + queryString : ""));
        request.setRawQueryPath(queryString);

        ODataResponse response = new ODataResponse();

        try {
            EdmProvider edmProvider = new EdmProvider(edmConfigLoader, serviceName);
            edmProvider.loadService();
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            ODataHandler handler = odata.createRawHandler(serviceMetadata);
            EntityCollectionProcessor processor = new EntityCollectionImp(quarkProcessor);
            handler.register(processor);
            response = handler.process(request);
        } catch (NotSupportedException e) {
            LOGGER.severe("Method not allowed: " + e.getMessage());
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        } catch (Exception e) {
            LOGGER.severe("Internal server error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Return the response
        return Response.status(response.getStatusCode())
                .entity(response.getContent())
                .type(response.getHeader(HttpHeader.CONTENT_TYPE).toString())
                .build();
    }
}
