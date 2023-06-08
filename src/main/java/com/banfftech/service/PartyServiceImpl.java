package com.banfftech.service;

import com.banfftech.model.GenericEntity;
import com.banfftech.model.Party;
import com.banfftech.model.Product;
import com.banfftech.model.SupplierProduct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

@ApplicationScoped
public class PartyServiceImpl implements PartyService{
    @Inject
    private Session session;

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

    @Override
    public Party get(String id) {
        Party party =  Party.findById(id);
        String hql = "from SupplierProduct sp left join Product p on sp.productId = p.id where sp.party = :party and p.productName like '%10100%' ";
        Query query = session.createQuery(hql, SupplierProduct.class);
        query.setParameter("party", party);
        List<GenericEntity> result = query.list();
        System.out.println(result);
        // write query again using CriteriaQuery
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupplierProduct> criteriaQuery = builder.createQuery(SupplierProduct.class);
        Root<SupplierProduct> root = criteriaQuery.from(SupplierProduct.class);
        Predicate partyRestriction = builder.equal(root.get("party"), party);
        Predicate productRestriction = builder.like(root.get("product").get("productName"), "%10100%");
        criteriaQuery.where(builder.and(partyRestriction, productRestriction));
        List<SupplierProduct> supplierProducts = session.createQuery(criteriaQuery).getResultList();
        System.out.println(supplierProducts);
        // write another sample using CriteriaQuery
        builder = session.getCriteriaBuilder();
        CriteriaQuery<Party> partyCriteriaQuery = builder.createQuery(Party.class);
        Root<Party> partyRoot = partyCriteriaQuery.from(Party.class);
        productRestriction = builder.any(partyRoot.get("product").get("productName").in("10100"));
        return party;
    }

}
