package com.example.iceandfire.controller;

import com.example.iceandfire.model.ApiResponse;
import com.example.iceandfire.model.House;
import com.example.iceandfire.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    /**
     * Q1 Main endpoint.
     * Fetches all houses, sorts them A–Z, writes houses_output.txt, returns JSON.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<House>>> getAllHouses() {
        try {
            List<House> houses = houseService.fetchSortAndExport();
            return ResponseEntity.ok(ApiResponse.ok(
                    "Fetched " + houses.size() + " houses, sorted alphabetically. File written: houses_output.txt",
                    houses
            ));
        } catch (IOException e) {
            log.error("Error in HouseController: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process houses: " + e.getMessage()));
        }
    }

    /**
     * Returns a summary of houses grouped by region.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSummaryByRegion() {
        try {
            List<House> houses = houseService.fetchSortAndExport();
            Map<String, Long> summary = houses.stream()
                    .collect(Collectors.groupingBy(
                            h -> h.getRegion() != null ? h.getRegion() : "Unknown",
                            Collectors.counting()
                    ));
            return ResponseEntity.ok(ApiResponse.ok("Houses grouped by region", summary));
        } catch (IOException e) {
            log.error("Error in HouseController summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to summarise houses: " + e.getMessage()));
        }
    }
}
