package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private MongoService mongoService;

    private List<Betaling> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sentToExternalSystem").is(false));
        return mongoService.getPayments(orgId, query);
    }

    private void updatePayment(String orgId, List<Betaling> payments) {
        payments.forEach(p -> updatePayment(orgId, p));
    }

    private void updatePayment(String orgId, Betaling payment) {
        Update update = new Update();
        update.set("fakturanummer", payment.getFakturanummer());
        update.set("restbelop", payment.getRestBelop());
        update.set("sentToExternalSystem", payment.isSentToExternalSystem());

        Query query = new Query();
        query.addCriteria(Criteria.where("ordrenummer").is(payment.getOrdrenummer()));

        mongoService.updatePayment(orgId, query, update);
    }
}
