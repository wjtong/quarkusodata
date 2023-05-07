package com.banfftech.resource;

import com.banfftech.model.party.party.Party;
import com.banfftech.service.PartyService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    public List<Party> list() {
        return Party.listAll();
    }

    @GET
    @Path("/{id}")
    public Party get(Long id) {
        return Party.findById(id);
    }

    @POST
//    @Transactional
    public Response create(Party party) {
        partyService.create(party);
//        party.persist();
        return Response.created(URI.create("/parties/" + party.id)).build();
    }

}
