package com.banfftech;

import com.banfftech.edmconfig.EdmEntityType;
import com.banfftech.edmconfig.EdmServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Path("/hello")
public class GreetingResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = Paths.get("config", "custRequestManage.json").toString();
        EdmServiceConfig edmServiceConfig = objectMapper.readValue(new File(filePath), EdmServiceConfig.class);
        System.out.println(edmServiceConfig.getServiceName() + "\n\r" + edmServiceConfig.getNameSpace());
        for (EdmEntityType edmEntityType:edmServiceConfig.getEntityTypes()) {
            System.out.println(edmEntityType.getEntityName() + ", " + edmEntityType.isAutoProperties());
        }
        return "Hello from RESTEasy Reactive";
    }
}
