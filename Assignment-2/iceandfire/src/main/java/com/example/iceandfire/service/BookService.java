package com.example.iceandfire.service;

import com.example.iceandfire.model.Book;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final IceAndFireApiClient apiClient;

    @Value("${api.iceandfire.base-url}")
    private String baseUrl;

    @Value("${output.books.csv}")
    private String outputFilePath;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Main entry point for Q2.
     * Fetches all books, builds the dictionary, and exports to CSV.
     *
     * @return Map of { bookName -> [pages, releaseDate, ISBN, publisher] }
     */
    public Map<String, List<String>> fetchBuildAndExport() throws IOException {
        // a) Read books from API
        log.info("=== Q2: Fetching all books from API ===");
        List<Book> books = apiClient.fetchAll(baseUrl + "/books", Book[].class);
        log.info("Total books fetched: {}", books.size());

        // b) Build dictionary
        Map<String, List<String>> booksDict = buildDictionary(books);
        log.info("Books dictionary built: {} entries", booksDict.size());

        // c) Write CSV
        writeToCsv(booksDict);

        return booksDict;
    }

    /**
     * Builds the book dictionary:
     * { bookName: [numberOfPages, dateOfRelease, ISBN, publisher] }
     */
    private Map<String, List<String>> buildDictionary(List<Book> books) {
        Map<String, List<String>> dict = new LinkedHashMap<>();

        for (Book book : books) {
            String name      = book.getName()      != null ? book.getName()      : "Unknown";
            String pages     = String.valueOf(book.getNumberOfPages());
            String released  = formatDate(book.getReleased());
            String isbn      = book.getIsbn()      != null ? book.getIsbn()      : "N/A";
            String publisher = book.getPublisher() != null ? book.getPublisher() : "N/A";

            dict.put(name, List.of(pages, released, isbn, publisher));
        }
        return dict;
    }

    /**
     * Parses an ISO-8601 date string and returns it in yyyy-MM-dd format.
     */
    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return "N/A";
        try {
            return OffsetDateTime.parse(rawDate).format(DATE_FMT);
        } catch (Exception e) {
            log.warn("Could not parse date: {}", rawDate);
            return rawDate;
        }
    }

    /**
     * Writes the books dictionary to a CSV file using OpenCSV.
     */
    private void writeToCsv(Map<String, List<String>> booksDict) throws IOException {
        File file = new File(outputFilePath);
        file.getParentFile().mkdirs();

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Header row
            writer.writeNext(new String[]{"Book Name", "Pages", "Date of Release", "ISBN", "Publisher"});

            // Data rows
            for (Map.Entry<String, List<String>> entry : booksDict.entrySet()) {
                List<String> vals = entry.getValue();
                writer.writeNext(new String[]{
                        entry.getKey(),
                        vals.get(0),    // pages
                        vals.get(1),    // release date
                        vals.get(2),    // isbn
                        vals.get(3),    // publisher
                });
            }
        }

        log.info("Books CSV written to: {}", outputFilePath);
    }
}
