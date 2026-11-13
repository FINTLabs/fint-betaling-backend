package no.fint.betaling.common.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.exception.SkoleressursException;
import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.betaling.fintdata.SchoolResourceRepository;
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator;
import no.novari.fint.model.resource.Link;
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.novari.fint.model.resource.felles.PersonResource;
import no.novari.fint.model.resource.okonomi.faktura.FakturaResource;
import no.novari.fint.model.resource.okonomi.faktura.FakturaResources;
import no.novari.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.novari.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.novari.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
        List<Link> directLinks = Optional.ofNullable(personalressurs.getSkoleressurs())
                .orElseGet(Collections::emptyList);

        return Flux.fromIterable(directLinks)
                .filter(link -> link != null && StringUtils.hasText(link.getHref()))
                .concatMap(link -> {
                    log.debug("Fetching skoleressurs from personalressurs link {}", link.getHref());
                    return restUtil.get(SkoleressursResource.class, link.getHref());
                })
                .next()
                .switchIfEmpty(Mono.fromCallable(() -> {
                    if (schoolResourceRepository.isEmpty()) {
                        schoolResourceRepository.update();
                    }
                    List<Link> selfLinks = Optional.ofNullable(personalressurs.getSelfLinks())
                            .orElseGet(Collections::emptyList);
                    return selfLinks.stream()
                            .map(schoolResourceRepository::getResourceByLink)
                            .flatMap(Optional::stream)
                            .findFirst()
                            .orElseThrow(() -> {
                                log.warn("No cached skoleressurs matched personalressurs self links {}", selfLinks);
                                return new SkoleressursException(HttpStatus.BAD_REQUEST,
                                        "Personalressursen har ingen relasjon til en skoleressurs");
                            });
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    public List<SkoleResource> getSkoler(SkoleressursResource skoleressurs) {
        Map<Link, SkoleResource> schools = schoolRepository.getMap();
        return skoleressurs
                .getSkole()
                .stream()
                .map(schools::get)
                .filter(Objects::nonNull)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());
    }

    public Mono<List<FakturaResource>> getFaktura(FakturagrunnlagResource invoice) {
        return Flux.fromIterable(invoice.getFaktura())
                .map(Link::getHref)
                .distinct()
                .concatMap(uri -> restUtil.getLive(FakturaResources.class, invoiceFilterUrl(uri))
                        .flatMapMany(resources -> Flux.fromIterable(resources.getContent())))
                .collectList();
    }

    private String invoiceFilterUrl(String href) {
        URI uri = URI.create(href);
        String path = uri.getPath();
        String marker = "/fakturanummer/";
        int index = path.lastIndexOf(marker);
        if (index < 0) {
            throw new IllegalArgumentException("Invoice link has no fakturanummer: " + href);
        }
        String invoiceNumber = path.substring(index + marker.length());
        return UriComponentsBuilder.fromUri(uri)
                .replacePath(path.substring(0, index))
                .replaceQuery(null)
                .fragment(null)
                .queryParam("$filter", "fakturanummer eq '" + invoiceNumber.replace("'", "''") + "'")
                .build().encode().toUriString();
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
