package com.example.iceandfire.controller;

import com.example.iceandfire.model.ApiResponse;
import com.example.iceandfire.model.Character;
import com.example.iceandfire.service.CharacterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<Character>>> getAllCharacters() {
        try {
            List<Character> characters = characterService.fetchSortAndExport();
            return ResponseEntity.ok(ApiResponse.ok(
                    "Fetched " + characters.size() + " characters sorted by seasons. File written: characters_output.xlsx",
                    characters
            ));
        } catch (IOException e) {
            log.error("Error in CharacterController: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process characters: " + e.getMessage()));
        }
    }


    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<Character>>> getTopCharacters(
            @RequestParam(defaultValue = "10") int n) {
        try {
            List<Character> all = characterService.fetchSortAndExport();
            List<Character> top = all.stream().limit(Math.max(1, n)).toList();
            return ResponseEntity.ok(ApiResponse.ok(
                    "Top " + top.size() + " characters by season appearances",
                    top
            ));
        } catch (IOException e) {
            log.error("Error in CharacterController top: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get top characters: " + e.getMessage()));
        }
    }
}

