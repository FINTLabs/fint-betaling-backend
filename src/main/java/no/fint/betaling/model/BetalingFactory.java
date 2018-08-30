package no.fint.betaling.model;

import no.fint.betaling.repository.OrderNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BetalingFactory {

    @Autowired
    private OrderNumberRepository orderNumberRepository;

    @Autowired
    private InvoiceFactory invoiceFactory;

    public List<Betaling> getBetaling(Payment payment, String orgId){
        return payment.getCustomers().stream().map(customer -> {
            Betaling betaling = new Betaling();
            betaling.setOrdrenummer(orderNumberRepository.getOrderNumber(orgId));
            betaling.setKunde(customer);
            betaling.setOppdragsgiver(payment.getEmployer());
            betaling.setVarelinjer(payment.getOrderLines());
            betaling.setTimeFrameDueDate(payment.getTimeFrameDueDate());
            betaling.setFakturagrunnlag(invoiceFactory.getInvoice(betaling));
            betaling.setRestBelop(betaling.getFakturagrunnlag().getNetto().toString());
            return betaling;
        }).collect(Collectors.toList());
    }
}
