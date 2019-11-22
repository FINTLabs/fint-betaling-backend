package no.fint.betaling.util

import no.fint.betaling.factory.InvoiceFactory
import no.fint.betaling.model.*
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource

import java.time.LocalDate

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

    static Principal newPrincipal() {
        return new Principal(
                uri: 'https://www.imdb.com/title/tt0093780/'.toURI(),
                description: 'The Principal (1987)',
                code: 'tt0093780',
                lineitems: [newLineitem()]
        )
    }

    static OrderItem newOrderItem() {
        return new OrderItem(
                lineitem: newLineitem(),
                description: 'Monkeyballs',
                itemQuantity: 1
        )
    }

    static Lineitem newLineitem() {
        return new Lineitem(
                description: 'Apple MacBook Pro',
                itemPrice: 1000000,
                itemCode: 'MBP',
                uri: 'link.to.Item'.toURI())
    }

    static Order newOrder() {
        return new Order(
                customers: [newCustomer()],
                principal: newPrincipal(),
                orderItems: [newOrderItem()],
                requestedNumberOfDaysToPaymentDeadline: '7'
        )
    }

    static Claim newClaim(String orderNumber, ClaimStatus claimStatus) {
        def order = newOrder()
        return new Claim(
                orderNumber: orderNumber,
                customer: newCustomer(),
                principal: newPrincipal(),
                requestedNumberOfDaysToPaymentDeadline: order.requestedNumberOfDaysToPaymentDeadline,
                originalAmountDue: order.sum(),
                orderItems: order.orderItems,
                claimStatus: claimStatus,
                createdDate: LocalDate.now(),
                invoiceUri: 'link.to.Invoice'.toURI(),
        )
    }

    static FakturagrunnlagResource newInvoice() {
        def invoice = InvoiceFactory.createInvoice(newClaim('12345', ClaimStatus.STORED))
        invoice.addSelf(Link.with('link.to.Invoice'))
        return invoice
    }
}
