package no.fint.betaling.util

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.model.OrderLine
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource

class BetalingObjectFactory {

    static Customer newCustomer() {
        Customer customer = new Customer()
        customer.setName('Ola Testesen')
        customer.setId('21i3v9')
        customer.setPostalAddress('Testeveien 13')
        customer.setPostalCode('1234')
        customer.setCity('Testeby')
        customer.setMobile('30960547')
        customer.setEmail('ola@testesen.no')
        customer.setPerson('link.to.Person'.toURI())
        return customer
    }

    static OrderLine newOrderLine() {
        OrderLine orderLine = new OrderLine()
        orderLine.setItemUri('link.to.Item'.toURI())
        orderLine.setDescription('Apple MacBook Pro')
        orderLine.setNumberOfItems(1)
        orderLine.setItemPrice(10000)
        return orderLine
    }

    static Order newOrder() {
        Order order = new Order()
        order.setCustomers([newCustomer()])
        order.setPrincipalUri('link.to.Principal'.toURI())
        order.setOrderLines([newOrderLine()])
        order.setRequestedNumberOfDaysToPaymentDeadline('7')
        return order
    }

    static Claim newClaim(String orderNumber, ClaimStatus claimStatus) {
        Claim claim = new Claim()
        claim.setOrderNumber(orderNumber);
        claim.setCustomer(newCustomer());
        claim.setPrincipalUri(newOrder().principalUri);
        claim.setRequestedNumberOfDaysToPaymentDeadline(newOrder().requestedNumberOfDaysToPaymentDeadline);
        claim.setOriginalAmountDue(newOrder().sum());
        claim.setOrderLines(newOrder().orderLines);
        claim.setClaimStatus(claimStatus);
        claim.invoiceUri = 'link.to.Invoice'.toURI()
        claim.claimStatus = ClaimStatus.STORED
        return claim
    }

    static FakturagrunnlagResource newInvoice() {
        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        Claim claim = newClaim('12345', ClaimStatus.STORED)
        invoice.setFakturalinjer([newInvoiceLine()]);
        invoice.setFakturadato(new Date());
        invoice.setForfallsdato(new Date());
        invoice.setLeveringsdato(new Date());
        invoice.setNetto(claim.getOriginalAmountDue());
        invoice.addMottaker(Link.with(claim.getCustomer().getPerson().toString()));
        invoice.addOppdragsgiver(Link.with(claim.getPrincipalUri().toString()));
        invoice.setOrdrenummer(new Identifikator(identifikatorverdi: claim.getOrderNumber()));
        invoice.addSelf(new Link(verdi: 'link.to.Invoice'))
        return invoice
    }

    static FakturalinjeResource newInvoiceLine() {
        FakturalinjeResource invoiceLine = new FakturalinjeResource();
        invoiceLine.setPris(newOrderLine().getItemPrice());
        invoiceLine.setAntall(newOrderLine().getNumberOfItems());
        invoiceLine.setFritekst([newOrderLine().getDescription()]);
        invoiceLine.addVarelinje(Link.with(newOrderLine().getItemUri().toString()));
        return invoiceLine;
    }
}
