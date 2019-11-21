package no.fint.betaling.util;

import lombok.SneakyThrows;

import java.net.URI;

public final class UriUtil {
    private UriUtil() {

    }

    @SneakyThrows
    public static URI parseUri(String uri) {
        return new URI(uri);
    }
}
