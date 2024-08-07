package no.fint.betaling.common.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.exception.SkoleressursException;
import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.betaling.fintdata.SchoolResourceRepository;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FintClient {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    private final SchoolRepository schoolRepository;

    private final SchoolResourceRepository schoolResourceRepository;

    public FintClient(RestUtil restUtil, Endpoints endpoints, SchoolRepository schoolRepository, SchoolResourceRepository schoolResourceRepository) {
        this.restUtil = restUtil;
        this.endpoints = endpoints;
        this.schoolRepository = schoolRepository;
        this.schoolResourceRepository = schoolResourceRepository;
    }

    public Mono<PersonalressursResource> getPersonalressurs(String ansattnummer) {

        return restUtil.get(
                PersonalressursResource.class,
                UriComponentsBuilder.fromUriString(endpoints.getEmployee()).pathSegment("ansattnummer", ansattnummer).build().toUriString()
        );
    }

    public Mono<PersonResource> getPerson(PersonalressursResource personalressurs) {
        return restUtil.get(
                PersonResource.class,
                personalressurs.getPerson().get(0).getHref()
        );
    }

    public Mono<SkoleressursResource> getSkoleressurs(PersonalressursResource personalressurs) {
        AtomicReference<SkoleressursResource> result = new AtomicReference<>();
        personalressurs.getSelfLinks()
                .stream()
                .map(schoolResourceRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(result::set);

        if (result.get() != null) return Mono.just(result.get());
        throw new SkoleressursException(HttpStatus.BAD_REQUEST, "Personalressursen har ingen relasjon til en skoleressurs");
    }

    public List<SkoleResource> getSkoler(SkoleressursResource skoleressurs) {
        return skoleressurs
                .getSkole()
                .stream()
                .map(schoolRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());
    }

    public Mono<List<FakturaResource>> getFaktura(FakturagrunnlagResource invoice) {
        return Flux.fromIterable(invoice.getFaktura())
                .map(Link::getHref)
                .flatMap(uri -> restUtil.get(FakturaResource.class, uri))
                .collectList();
    }

    public Set<String> getFakturanummere(List<FakturaResource> fakturaList) {
        return fakturaList.stream()
                .map(FakturaResource::getFakturanummer)
                .map(Identifikator::getIdentifikatorverdi)
                .collect(Collectors.toSet());
    }

    public Optional<LocalDate> getInvoiceDate(List<FakturaResource> fakturaList) {
        return fakturaList.stream()
                .map(FakturaResource::getDato)
                .filter(Objects::nonNull)
                .map(date -> date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> getPaymentDueDate(List<FakturaResource> fakturaList) {
        return fakturaList.stream()
                .map(FakturaResource::getForfallsdato)
                .filter(Objects::nonNull)
                .map(date -> date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .max(Comparator.naturalOrder());
    }

    public Optional<Long> getAmountDue(List<FakturaResource> fakturaList) {
        return fakturaList.stream()
                .map(FakturaResource::getRestbelop)
                .filter(Objects::nonNull)
                .reduce(Long::sum);

    }

    public Optional<String> getSelfLink(FakturagrunnlagResource invoice) {
        return invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny();
    }
}
