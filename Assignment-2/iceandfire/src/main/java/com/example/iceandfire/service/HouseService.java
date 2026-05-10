package com.example.iceandfire.service;

import com.example.iceandfire.model.House;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseService {

    private final IceAndFireApiClient apiClient;

    @Value("${api.iceandfire.base-url}")
    private String baseUrl;

    @Value("${output.houses.txt}")
    private String outputFilePath;

    /**
     * Main entry point for Q1.
     * Fetches all houses, sorts them alphabetically, and writes to a text file.
     *
     * @return sorted list of House objects
     */
    public List<House> fetchSortAndExport() throws IOException {
        // a) Fetch all houses from API
        log.info("=== Q1: Fetching all houses from API ===");
        List<House> houses = apiClient.fetchAll(baseUrl + "/houses", House[].class);
        log.info("Total houses fetched: {}", houses.size());

        // c) Sort alphabetically by house name (case-insensitive)
        houses.sort(Comparator.comparing(
                h -> h.getName() != null ? h.getName().toLowerCase() : ""
        ));
        log.info("Houses sorted alphabetically.");

        // b) Write to text file
        writeToTextFile(houses);

        return houses;
    }

    /**
     * Writes the sorted house list to a formatted text file.
     */
    private void writeToTextFile(List<House> houses) throws IOException {
        File file = new File(outputFilePath);
        file.getParentFile().mkdirs();  // create output directory if needed

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Houses of Ice and Fire — Sorted Alphabetically");
            writer.newLine();
            writer.write("=".repeat(70));
            writer.newLine();
            writer.newLine();
            writer.write(String.format("%-6s %-45s %s", "#", "House Name", "Region"));
            writer.newLine();
            writer.write("-".repeat(80));
            writer.newLine();

            for (int i = 0; i < houses.size(); i++) {
                House h = houses.get(i);
                String name   = h.getName()   != null ? h.getName()   : "Unknown";
                String region = h.getRegion() != null ? h.getRegion() : "Unknown Region";
                writer.write(String.format("%-6d %-45s %s", i + 1, name, region));
                writer.newLine();
            }
        }

        log.info("Houses written to: {}", outputFilePath);
    }
}