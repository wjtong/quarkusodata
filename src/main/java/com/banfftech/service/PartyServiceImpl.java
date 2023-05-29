package com.banfftech.service;

import com.banfftech.model.Party;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PartyServiceImpl implements PartyService{
    @Override
    @Transactional
    public Party create(Party party) {
        party.persist();
        return party;
    }

    @Override
    public List<Party> list() {
        return Party.listAll();
    }
}
