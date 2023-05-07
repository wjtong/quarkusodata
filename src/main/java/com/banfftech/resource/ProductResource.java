package com.banfftech.resource;

import com.banfftech.model.product.product.Product;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    @GET
    public List<Product> list() {
        return Product.listAll();
    }

    @GET
    @Path("/{id}")
    public Product get(String id) {
        return Product.findById(id);
    }

    @POST
    @Transactional
    public Response create(Product product) {
        product.persist();
        return Response.created(URI.create("/products/" + product.getPk())).build();
    }
}
