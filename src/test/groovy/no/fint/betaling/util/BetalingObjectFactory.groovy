package no.fint.betaling.util

import groovy.time.TimeCategory
import no.fint.betaling.model.*
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.faktura.FakturaResource
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

class BetalingObjectFactory {

    static Customer newCustomer() {
        return new Customer(
                name: 'Ola Testesen',
                id: '21i3v9')
    }

    static Principal newPrincipal() {
        return new Principal(
                uri: 'https://www.imdb.com/title/tt0093780/',
                description: 'The Principal (1987)',
                code: 'tt0093780',
                lineitems: ['MBP']
        )
    }

    static OrderItem newOrderItem() {
        return new OrderItem(
                description: 'Monkeyballs',
                originalDescription: 'Monkeyballs Original',
                originalItemPrice: 1000000,
                itemCode: 'MBP',
                itemUri: 'link.to.Item',
                itemQuantity: 1
        )
    }

    static Lineitem newLineitem() {
        return new Lineitem(
                description: 'Apple MacBook Pro',
                itemPrice: 1000000,
                itemCode: 'MBP',
                uri: 'link.to.Item')
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

    static Claim newClaim(long orderNumber, ClaimStatus claimStatus) {
        def order = newOrder()
        return new Claim(
                orgId: '123',
                orderNumber: orderNumber,
                invoiceNumbers: ['456'],
                invoiceDate: LocalDate.parse('2019-11-10'),
                paymentDueDate: LocalDate.parse('2019-11-17'),
                createdDate: LocalDateTime.parse('2019-11-01T00:00:00'),
                lastModifiedDate: LocalDateTime.parse('2019-11-05T00:00:00'), creditNotes: [newCreditNote()],
                amountDue: 500000,
                originalAmountDue: order.sum(),
                requestedNumberOfDaysToPaymentDeadline: order.requestedNumberOfDaysToPaymentDeadline,
                customerId: String,
                customerName: 'Ola Testesen',
                createdByEmployeeNumber: newUser().getEmployeeNumber(),
                organisationUnit: newOrganisationUnit(),
                principalCode: newPrincipal().getCode(),
                principalUri: newPrincipal().getUri(),
                invoiceUri: 'link.to.Invoice',
                orderItems: order.orderItems,
                claimStatus: claimStatus,
        )
    }

    static CreditNote newCreditNote() {
        return new CreditNote(
                id: '111',
                date: LocalDateTime.parse('2019-12-11T00:00:00'),
                amount: 500000,
                comment: 'This is a comment'
        )
    }

    static User newUser() {
        return new User(
                name: 'Frank Testesen',
                employeeNumber: '2001',
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
        def invoice = new FakturagrunnlagResource(ordrenummer: new Identifikator(identifikatorverdi: '12345'))
        invoice.addSelf(Link.with('link.to.Invoice'))
        return invoice
    }

    static FakturaResource newFaktura() {
        ThreadLocalRandom r = ThreadLocalRandom.current()
        def faktura = new FakturaResource()

        faktura.setBetalt(r.nextBoolean())
        faktura.setFakturert(r.nextBoolean())
        faktura.setKreditert(r.nextBoolean())
        faktura.setBelop(r.nextLong(50000, 100000))
        faktura.setRestbelop(r.nextLong(10000, 50000))
        faktura.setFakturanummer(new Identifikator(identifikatorverdi: r.nextLong(100000, 3000000)))
        faktura.setDato(new Date(2024,1,3))
        use(TimeCategory) {
            def now = new Date()
            faktura.setDato(now - 12.days)
            faktura.setForfallsdato(now + 14.days)
        }
        return faktura
    }
}
