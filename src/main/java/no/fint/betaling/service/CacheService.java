package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CacheService {

    @Autowired
    private RestUtil restUtil;

    private final Map<String, Long> lastUpdatedMap = Collections.synchronizedMap(new HashMap<>());

    public <T> T getUpdates(Class<T> type, String url, String orgId) {
        String key = orgId + "_" + url;
        long lastUpdated = Long.parseLong(restUtil.get(Map.class, url + "/last-updated", orgId).get("lastUpdated").toString());
        long since = lastUpdatedMap.getOrDefault(key, -1L) + 1L;
        log.info("{}: Fetching {} since {}, last updated {} ...", orgId, url, since, lastUpdated);
        T result = restUtil.get(type, UriComponentsBuilder.fromUriString(url).queryParam("sinceTimeStamp", since).build().toUriString(), orgId);
        lastUpdatedMap.put(key, lastUpdated);
        return result;
    }

}
