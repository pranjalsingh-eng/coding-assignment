package com.example.iceandfire.util;

import com.example.iceandfire.model.House;
import com.example.iceandfire.model.Character;
import com.example.iceandfire.service.BookService;
import com.example.iceandfire.service.CharacterService;
import com.example.iceandfire.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentRunner implements CommandLineRunner {

    private final HouseService houseService;
    private final BookService bookService;
    private final CharacterService characterService;

    @Override
    public void run(String... args) throws Exception {
        log.info("");
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║     Ice & Fire API Assignment — Calsoft Pvt Ltd          ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("");

        // ── Q1: Houses ────────────────────────────────────────────────────
        try {
            log.info("▶  Running Q1: Houses of Ice and Fire...");
            List<House> houses = houseService.fetchSortAndExport();
            log.info("✔  Q1 complete — {} houses → output/houses_output.txt", houses.size());
        } catch (Exception e) {
            log.error("✘  Q1 failed: {}", e.getMessage());
        }

        log.info("");

        // ── Q2: Books ─────────────────────────────────────────────────────
        try {
            log.info("▶  Running Q2: Books of Ice and Fire...");
            Map<String, List<String>> books = bookService.fetchBuildAndExport();
            log.info("✔  Q2 complete — {} books → output/books_output.csv", books.size());
        } catch (Exception e) {
            log.error("✘  Q2 failed: {}", e.getMessage());
        }

        log.info("");

        // ── Q3: Characters ────────────────────────────────────────────────
        try {
            log.info("▶  Running Q3: Characters of Ice and Fire...");
            List<Character> characters = characterService.fetchSortAndExport();
            log.info("✔  Q3 complete — {} characters → output/characters_output.xlsx", characters.size());
        } catch (Exception e) {
            log.error("✘  Q3 failed: {}", e.getMessage());
        }

        log.info("");
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  All tasks complete. REST API available at:              ║");
        log.info("║    http://localhost:8080/api/houses                      ║");
        log.info("║    http://localhost:8080/api/books                       ║");
        log.info("║    http://localhost:8080/api/characters                  ║");
        log.info("║    http://localhost:8080/api/characters/top?n=10         ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("");
    }
}

