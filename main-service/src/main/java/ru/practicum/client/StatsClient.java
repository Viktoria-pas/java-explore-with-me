package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class StatsClient {

    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String statsServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(statsServiceUrl)
                .build();
    }

    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        try {

            EndPointHitDto hitDto = new EndPointHitDto(app, uri, ip, timestamp);

            webClient.post()
                    .uri("/hit")
                    .bodyValue(hitDto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            result -> log.debug("Hit saved successfully for uri: {}", uri),
                            error -> log.error("Error saving hit for uri: {}", uri, error)
                    );
        } catch (Exception e) {
            log.error("Error creating hit for uri: {}", uri, e);
        }
    }

    public Mono<List<ViewStatsDto>> getStats(LocalDateTime start, LocalDateTime end,
                                             List<String> uris, Boolean unique) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/stats")
                            .queryParam("start", start.format(FORMATTER))
                            .queryParam("end", end.format(FORMATTER));

                    if (uris != null && !uris.isEmpty()) {
                        builder.queryParam("uris", String.join(",", uris));
                    }

                    if (unique != null) {
                        builder.queryParam("unique", unique);
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .doOnError(error -> log.error("Error getting stats", error))
                .onErrorReturn(List.of());
    }
}
