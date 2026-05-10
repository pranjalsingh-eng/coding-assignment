package com.example.iceandfire.controller;

import com.example.iceandfire.model.ApiResponse;
import com.example.iceandfire.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Q2 Main endpoint.
     * Fetches all books, builds the dictionary, writes books_output.csv, returns JSON.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getAllBooks() {
        try {
            Map<String, List<String>> booksDict = bookService.fetchBuildAndExport();
            return ResponseEntity.ok(ApiResponse.ok(
                    "Fetched " + booksDict.size() + " books. Dictionary built. File written: books_output.csv",
                    booksDict
            ));
        } catch (IOException e) {
            log.error("Error in BookController: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process books: " + e.getMessage()));
        }
    }
}
