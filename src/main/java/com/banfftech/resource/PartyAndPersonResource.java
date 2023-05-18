package com.banfftech.resource;

import com.banfftech.model.Party;
import com.banfftech.model.PartyAndPerson;
import com.banfftech.model.Person;
import com.banfftech.model.Product;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/partyandperson")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartyAndPersonResource {
    @GET
    public List<PartyAndPerson> list() {
        return PartyAndPerson.listAll();
    }

    @GET
    @Path("/{id}")
    public PartyAndPerson get(String id) {
        return PartyAndPerson.findById(id);
    }

    @POST
    @Transactional
    public Response create(PartyAndPerson partyAndPerson) {
        Party party = new Party();
        party.externalId = partyAndPerson.externalId;
        party.partyTypeId = "PERSON";
        party.persist();
        Person person = new Person();
        person.partyId = party.id;
        person.lastName = partyAndPerson.lastName;
        person.persist();
        return Response.created(URI.create("/partyandperson/" + party.getPk())).build();
    }
}
