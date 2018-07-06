package no.fint.betaling.model;

import no.fint.betaling.service.OrderNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BetalingFactory {

    @Autowired
    private OrderNumberService orderNumberService;

    public List<Betaling> getBetaling(Payment payment, String orgId){
        return payment.getCustomers().stream().map(customer -> {
            Betaling betaling = new Betaling();
            betaling.setOrdrenummer(orderNumberService.getOrderNumber(orgId));
            betaling.setKunde(customer);
            betaling.setOppdragsgiver(payment.getEmployer());
            betaling.setVarelinjer(payment.getOrderLines());
            betaling.setOppdragsgiver(payment.getEmployer());
            betaling.setTimeFrameDueDate(payment.getTimeFrameDueDate());
            betaling.setFakturagrunnlag(InvoiceFactory.getInvoice(betaling));
            return betaling;
        }).collect(Collectors.toList());
    }
}
