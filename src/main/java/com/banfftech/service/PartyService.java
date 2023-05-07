package com.banfftech.service;

import com.banfftech.model.party.party.Party;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@ApplicationScoped
public class PartyService {
    @Transactional
    public Party create(Party party) {
        party.persist();
        return party;
    }
}
