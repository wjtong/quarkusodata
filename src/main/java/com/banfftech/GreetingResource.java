package com.banfftech;

import com.banfftech.odata.EdmConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Path("/hello")
public class GreetingResource {
    @Inject
    EdmConfig edmconfig;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        System.out.println(edmconfig.services());
        return "Hello from RESTEasy Reactive";
    }
}
