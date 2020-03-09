package no.fint.betaling.util

import no.fint.betaling.factory.InvoiceFactory
import no.fint.betaling.model.*
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.FakturaResource
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource

import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom

class BetalingObjectFactory {

    static Customer newCustomer() {
        return new Customer(
                name: 'Ola Testesen',
                id: '21i3v9',
                postalAddress: 'Testeveien 13',
                postalCode: '1234',
                city: 'Testeby',
                mobile: '30960547',
                email: 'ola@testesen.no',
                person: 'link.to.Person'.toURI())
    }

    static Principal newPrincipal() {
        return new Principal(
                uri: 'https://www.imdb.com/title/tt0093780/'.toURI(),
                description: 'The Principal (1987)',
                code: 'tt0093780',
                lineitems: ['MBP']
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
                requestedNumberOfDaysToPaymentDeadline: '7',
                organisationUnit: newOrganisationUnit(),
                createdBy: newUser()
        )
    }

    static Claim newClaim(String orderNumber, ClaimStatus claimStatus) {
        def order = newOrder()
        return new Claim(
                orgId: '123',
                orderNumber: orderNumber,
                invoiceNumbers: ['456'],
                invoiceDate: LocalDate.parse('2019-11-10'),
                paymentDueDate: LocalDate.parse('2019-11-17'),
                createdDate: LocalDate.parse('2019-11-01'),
                lastModifiedDate: LocalDate.parse('2019-11-05'),
                creditNotes: [newCreditNote()],
                amountDue: 500000,
                originalAmountDue: order.sum(),
                requestedNumberOfDaysToPaymentDeadline: order.requestedNumberOfDaysToPaymentDeadline,
                customer: newCustomer(),
                createdBy: newUser(),
                organisationUnit: newOrganisationUnit(),
                principal: newPrincipal(),
                invoiceUri: 'link.to.Invoice'.toURI(),
                orderItems: order.orderItems,
                claimStatus: claimStatus,
        )
    }

    static CreditNote newCreditNote() {
        return new CreditNote(
                id: '111',
                date: LocalDate.parse('2019-11-12'),
                amount: 500000,
                comment: 'This is a comment'
        )
    }

    static User newUser() {
        return new User(
                name: 'Frank Testesen',
                organisation: newOrganisation(),
                organisationUnits: [newOrganisationUnit()]
        )
    }

    static Organisation newOrganisation() {
        return new Organisation(
                name: 'SVH',
                organisationNumber: 'NO987654321'
        )
    }

    static Organisation newOrganisationUnit() {
        return new Organisation(
                name: 'HVS',
                organisationNumber: 'NO123456789'
        )
    }

    static FakturagrunnlagResource newFakturagrunnlag() {
        def invoice = InvoiceFactory.createInvoice(newClaim('12345', ClaimStatus.STORED))
        invoice.addSelf(Link.with('link.to.Invoice'))
        return invoice
    }

    static FakturaResource newFaktura() {
        ThreadLocalRandom r = ThreadLocalRandom.current()
        def faktura = new FakturaResource()
        faktura.setBetalt(r.nextBoolean())
        faktura.setFakturert(r.nextBoolean())
        faktura.setKreditert(r.nextBoolean())
        faktura.setFakturabelop(r.nextLong(50000, 100000))
        faktura.setRestbelop(r.nextLong(10000,50000))
        faktura.setFakturanummer(new Identifikator(identifikatorverdi: r.nextLong(100000, 3000000)))
        faktura.setFakturadato(new Date() - r.nextInt(0, 10))
        faktura.setForfallsdato(new Date() + r.nextInt(0, 10))
        return faktura
    }
}
