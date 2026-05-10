package com.example.iceandfire.service;

import com.example.iceandfire.model.Character;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final IceAndFireApiClient apiClient;

    @Value("${api.iceandfire.base-url}")
    private String baseUrl;

    @Value("${output.characters.excel}")
    private String outputFilePath;

    // Excel colour constants (ARGB hex)
    private static final String HEADER_BG   = "FF1F4E79";
    private static final String ALT_ROW_BG  = "FFD6E4F0";
    private static final String HEADER_FG   = "FFFFFFFF";

    /**
     * Main entry point for Q3.
     *
     * @return sorted list of characters
     */
    public List<Character> fetchSortAndExport() throws IOException {
        // a) Fetch all characters
        log.info("=== Q3: Fetching all characters from API ===");
        List<Character> characters = apiClient.fetchAll(baseUrl + "/characters", Character[].class);
        log.info("Total characters fetched: {}", characters.size());

        // b & c) Sort by season count descending
        characters.sort(Comparator.comparingInt(Character::getSeasonCount).reversed());
        log.info("Characters sorted by season appearances (descending).");

        // Log top 10
        log.info("Top 10 characters by season appearances:");
        characters.stream().limit(10).forEach(c ->
                log.info("  {} — {} season(s)", c.getDisplayName(), c.getSeasonCount())
        );

        // d) Export to Excel
        writeToExcel(characters);

        return characters;
    }

    /**
     * Writes all character data to a formatted Excel file using Apache POI.
     */
    private void writeToExcel(List<Character> characters) throws IOException {
        File file = new File(outputFilePath);
        file.getParentFile().mkdirs();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            XSSFSheet sheet = workbook.createSheet("Characters");

            // ── Cell Styles ─────────────────────────────────────────────
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle altRowStyle  = createAltRowStyle(workbook);
            CellStyle normalStyle  = createNormalStyle(workbook);

            // ── Header Row ───────────────────────────────────────────────
            String[] headers = {
                    "Name", "Gender", "Culture", "Born", "Died",
                    "Season Count", "TV Seasons", "Aliases", "Titles",
                    "Books Count", "POV Books", "Played By"
            };
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data Rows ────────────────────────────────────────────────
            for (int i = 0; i < characters.size(); i++) {
                Character c = characters.get(i);
                Row row = sheet.createRow(i + 1);
                CellStyle rowStyle = (i % 2 == 1) ? altRowStyle : normalStyle;

                setCellValue(row, 0,  c.getDisplayName(),                    rowStyle);
                setCellValue(row, 1,  nullSafe(c.getGender()),               rowStyle);
                setCellValue(row, 2,  nullSafe(c.getCulture()),              rowStyle);
                setCellValue(row, 3,  nullSafe(c.getBorn()),                 rowStyle);
                setCellValue(row, 4,  nullSafe(c.getDied(), "Alive"),        rowStyle);
                setCellNumeric(row, 5, c.getSeasonCount(),                   rowStyle);
                setCellValue(row, 6,  joinList(c.getTvSeries()),             rowStyle);
                setCellValue(row, 7,  joinList(c.getAliases()),              rowStyle);
                setCellValue(row, 8,  joinList(c.getTitles()),               rowStyle);
                setCellNumeric(row, 9,  countList(c.getBooks()),             rowStyle);
                setCellNumeric(row, 10, countList(c.getPovBooks()),          rowStyle);
                setCellValue(row, 11, joinList(c.getPlayedBy()),             rowStyle);
            }

            // ── Column Widths (in units of 1/256 of a character width) ───
            int[] colWidths = {7000, 2500, 3500, 4500, 4500, 3500, 12000, 7000, 8000, 3200, 3200, 6000};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i]);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            // Enable auto-filter on header
            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1)
            );

            workbook.write(fos);
        }

        log.info("Characters Excel written to: {}", outputFilePath);
    }

    // ── Style Factories ──────────────────────────────────────────────────────

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(HEADER_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBottomBorderColor(new XSSFColor(hexToBytes("FF2E75B6"), null));

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(hexToBytes(HEADER_FG), null));
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private CellStyle createAltRowStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(ALT_ROW_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(hexToBytes("FFBDC3C7"), null));
        return style;
    }

    private CellStyle createNormalStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(hexToBytes("FFBDC3C7"), null));
        return style;
    }

    // ── Cell Setters ─────────────────────────────────────────────────────────

    private void setCellValue(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setCellNumeric(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // ── Utility Methods ───────────────────────────────────────────────────────

    private String nullSafe(String s) {
        return (s != null && !s.isBlank()) ? s : "Unknown";
    }

    private String nullSafe(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "None";
        return String.join(", ", list.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList());
    }

    private int countList(List<String> list) {
        if (list == null) return 0;
        return (int) list.stream().filter(s -> s != null && !s.isBlank()).count();
    }

    /** Convert ARGB hex string (e.g. "FF1F4E79") to byte array for XSSFColor. */
    private byte[] hexToBytes(String argbHex) {
        int len = argbHex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((java.lang.Character.digit(argbHex.charAt(i), 16) << 4)
                    + java.lang.Character.digit(argbHex.charAt(i + 1), 16));
        }
        return data;
    }
}

