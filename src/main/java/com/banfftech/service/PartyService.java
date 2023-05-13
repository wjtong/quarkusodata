package com.banfftech.service;

import com.banfftech.model.Party;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PartyService {
    @Transactional
    public Party create(Party party) {
        party.persist();
        return party;
    }
}
