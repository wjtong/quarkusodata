package com.banfftech.resource;

import com.banfftech.model.Party;
import com.banfftech.service.PartyService;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/parties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartyResrouce {
    @Inject
    PartyService partyService;

    @GET
    @PermitAll
    public List<Party> list() {
        return partyService.list();
    }

    @GET
    @Path("/{id}")
    public Party get(String id) {
        return partyService.get(id);
    }

    @POST
//    @Transactional
    public Response create(Party party) {
        partyService.create(party);
//        party.persist();
        return Response.created(URI.create("/parties/" + party.id)).build();
    }

}
