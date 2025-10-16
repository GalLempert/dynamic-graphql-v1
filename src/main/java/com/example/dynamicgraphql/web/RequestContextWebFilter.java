package com.example.dynamicgraphql.web;

import com.example.dynamicgraphql.context.RequestContext;
import com.example.dynamicgraphql.context.RequestContext.GeoFormat;
import com.example.dynamicgraphql.context.RequestContext.TimeFormat;
import com.example.dynamicgraphql.context.RequestContextHolder;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestContextWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String requestId = headers.getFirst("X-Request-Id");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        TimeFormat timeFormat = RequestContext.parseTimeFormat(headers.getFirst("X-Time-Format"));
        GeoFormat geoFormat = RequestContext.parseGeoFormat(headers.getFirst("X-Geo-Format"));
        Locale locale = headers.getAcceptLanguageAsLocales().stream().findFirst().orElse(Locale.getDefault());
        RequestContext context = new RequestContext(requestId, timeFormat, geoFormat, locale);
        exchange.getResponse().getHeaders().add("X-Request-Id", requestId);
        log.debug("Request {} using timeFormat={} geoFormat={}", requestId, timeFormat, geoFormat);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(RequestContext.class, context))
                .doOnSubscribe(ignored -> RequestContextHolder.set(context))
                .doFinally(signal -> RequestContextHolder.clear());
    }
}
