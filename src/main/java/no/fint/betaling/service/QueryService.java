package no.fint.betaling.service;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class QueryService {

    private final String orgId;

    public QueryService(
            @Value("${fint.betaling.org-id}") String orgId) {
        this.orgId = orgId;
    }

    public Query createQuery() {
        return new Query().addCriteria(Criteria.where("_class").is(Claim.class.getName())).addCriteria(Criteria.where("orgId").is(orgId));
    }

    public Query queryByOrderNumber(String orderNumber) {
        return createQuery().addCriteria(Criteria.where("orderNumber").is(orderNumber));
    }

    public Query queryByClaimStatus(ClaimStatus... statuses) {
        return createQuery().addCriteria(Criteria.where("claimStatus").in(Arrays.asList(statuses)));
    }

    public Query queryByCustomerNameRegex(String name) {
        return createQuery().addCriteria(Criteria.where("customer.name").regex(name, "i"));
    }
}
