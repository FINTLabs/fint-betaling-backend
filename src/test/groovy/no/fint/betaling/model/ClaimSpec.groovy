package no.fint.betaling.model

import spock.lang.Specification

class ClaimSpec extends Specification {

    def "getInvoiceNumbers returns empty set when invoiceNumbersCommaSeperated is null"() {
        given: "a claim with null invoiceNumbersCommaSeperated"
        Claim claim = new Claim()

        expect: "getInvoiceNumbers returns an empty set"
        claim.invoiceNumbersCommaSeperated == null
        claim.getInvoiceNumbers().isEmpty()
    }

    def "getInvoiceNumbers returns a set of invoice numbers"() {
        given: "a claim with comma separated invoice numbers"
        Claim claim = new Claim()
        claim.invoiceNumbersCommaSeperated = "INV123,INV456,INV789"

        expect: "getInvoiceNumbers returns a set with those invoice numbers"
        claim.getInvoiceNumbers() == new HashSet(["INV123", "INV456", "INV789"])
    }

    def "setInvoiceNumbers sets invoiceNumbersCommaSeperated from a set"() {
        given: "a set of invoice numbers"
        Set<String> invoiceNumbers = new HashSet<>()
        invoiceNumbers.addAll(["INV123", "INV456", "INV789"])
        Claim claim = new Claim()

        when: "setInvoiceNumbers is called with the set"
        claim.setInvoiceNumbers(invoiceNumbers)

        then: "invoiceNumbersCommaSeperated is set to the comma separated string of the set"
        claim.invoiceNumbersCommaSeperated.contains("INV123")
        claim.invoiceNumbersCommaSeperated.contains("INV456")
        claim.invoiceNumbersCommaSeperated.contains("INV789")
    }

    def "setInvoiceNumbers followed by getInvoiceNumbers round trip"() {
        given: "a set of invoice numbers"
        Set<String> originalInvoiceNumbers = new HashSet<>()
        originalInvoiceNumbers.addAll(["INV123", "INV456", "INV789"])
        Claim claim = new Claim()

        when: "setInvoiceNumbers is called and then getInvoiceNumbers"
        claim.setInvoiceNumbers(originalInvoiceNumbers)
        Set<String> roundTripInvoiceNumbers = claim.getInvoiceNumbers()

        then: "getInvoiceNumbers returns a set equivalent to the original"
        roundTripInvoiceNumbers == originalInvoiceNumbers
    }
}
