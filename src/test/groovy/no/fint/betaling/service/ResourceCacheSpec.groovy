package no.fint.betaling.service

import no.fint.betaling.common.ResourceCache
import no.fint.betaling.common.util.RestUtil
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.vurdering.ElevfravarResource
import no.fint.model.resource.utdanning.vurdering.ElevfravarResources
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.function.Function

class ResourceCacheSpec extends Specification {

    private RestUtil restUtil

    def setup() {
        restUtil = Mock(RestUtil)
    }

    def "update with self link - success"() {
        given:
        String endpoint = "https://example.com/resources"
        ResourceCache<ElevfravarResource, ElevfravarResources> resourceCache = new ResourceCache<>(restUtil, endpoint, ElevfravarResource.class)

        ElevfravarResources elevfravarResources = new ElevfravarResources()
        elevfravarResources.addResource(createResource("test-url-1"))
        elevfravarResources.addResource(createResource("test-url-2"))
        restUtil.getWithRetry(_, _) >> Mono.just(elevfravarResources)

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.get()
        resources.size() == 2
        resources.containsKey(new Link("test-url-1"))
        resources.containsKey(new Link("test-url-2"))
    }

    def "update with custom link - success"() {
        given:
        String endpoint = "https://example.com/resources"
        Function<ElevfravarResource, List<Link>> linkProvider = { resource -> resource.getElevforhold() }
        ResourceCache<ElevfravarResource, ElevfravarResources> resourceCache = new ResourceCache<>(restUtil, endpoint, ElevfravarResource.class, linkProvider)

        ElevfravarResources elevfravarResources = new ElevfravarResources()
        elevfravarResources.addResource(createResource("test-url-1", "Test1"))
        elevfravarResources.addResource(createResource("test-url-2", "Test2"))
        restUtil.getWithRetry(_, _) >> Mono.just(elevfravarResources)

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.get()
        resources.size() == 2
        resources.containsKey(new Link("Test1"))
        resources.containsKey(new Link("Test2"))
    }


    private ElevfravarResource createResource(String href) {
        return createResource(href, null)
    }

    private ElevfravarResource createResource(String href, String elevforhold) {
        ElevfravarResource resource = new ElevfravarResource()
        resource.addSelf(new Link(href))
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(elevforhold))
            resource.addElevforhold(new Link(elevforhold))
        return resource
    }

    def "update - no items"() {
        given:
        String endpoint = "https://example.com/resources"
        ResourceCache<ElevfravarResource, ElevfravarResources> resourceCache = new ResourceCache<>(restUtil, endpoint, ElevfravarResource.class)

        restUtil.getWithRetry(_, _) >> Mono.just(new ElevfravarResources())

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.get()
        resources.isEmpty()
    }

    def "update - error"() {
        given:
        String endpoint = "https://example.com/resources"
        ResourceCache<ElevfravarResource, ElevfravarResources> resourceCache = new ResourceCache<>(restUtil, endpoint, ElevfravarResource.class)

        WebClientResponseException exception = Mock(WebClientResponseException)
        restUtil.getWithRetry(_, _) >> { throw exception }

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.get()
        resources.isEmpty()
    }
}
