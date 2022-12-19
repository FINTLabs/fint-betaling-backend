package no.fint.betaling.service;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Arrays;
import java.util.Date;

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

    public Query queryByClaimStatusByMaximumDaysOld(long maximumDaysOld, ClaimStatus... statuses) {
        LocalDateTime date = LocalDateTime.now().plusDays(-1 * maximumDaysOld);

        return createQuery()
                .addCriteria(Criteria.where("claimStatus").in(Arrays.asList(statuses)))
                .addCriteria(Criteria.where("lastModifiedDate").gte(date));
    }

    public Query queryByCustomerNameRegex(String name) {
        return createQuery().addCriteria(Criteria.where("customer.name").regex(name, "i"));
    }

    public Query queryByDateAndSchoolAndStatus(Date date, String schoolOrganisationNumber, ClaimStatus... statuses) {
        Query query = createQuery();

        if (date != null)
            query.addCriteria(Criteria.where("createdDate").gte(date));
        if (StringUtils.isNotBlank(schoolOrganisationNumber))
            query.addCriteria(Criteria.where("organisationUnit.organisationNumber").is(schoolOrganisationNumber));
        if (statuses != null && statuses.length > 0)
            query.addCriteria(Criteria.where("claimStatus").in(Arrays.asList(statuses)));

        return query;
    }
}
