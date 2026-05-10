package com.example.iceandfire.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IceAndFireApiClient {

    private final RestTemplate restTemplate;

    @Value("${api.iceandfire.page-size:50}")
    private int pageSize;

    public <T> List<T> fetchAll(String endpoint, Class<T[]> itemClass) {
        List<T> results = new ArrayList<>();
        int page = 1;

        log.info("Starting paginated fetch from: {}", endpoint);

        while (true) {
            String url = UriComponentsBuilder.fromUri(URI.create(endpoint))
                    .queryParam("page", page)
                    .queryParam("pageSize", pageSize)
                    .toUriString();

            log.debug("Fetching page {} → {}", page, url);

            T[] pageData = restTemplate.getForObject(url, itemClass);

            if (pageData == null || pageData.length == 0) {
                log.info("No more data at page {}. Total records: {}", page, results.size());
                break;
            }

            results.addAll(Arrays.asList(pageData));
            log.info("Page {} fetched: {} records (total so far: {})", page, pageData.length, results.size());

            if (pageData.length < pageSize) {
                // Last page reached
                break;
            }
            page++;
        }

        return results;
    }
}
