package com.banfftech.service;

import com.banfftech.model.Party;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PartyService {
    Party create(Party party);
    List<Party> list();
    Party get(String id);
}
