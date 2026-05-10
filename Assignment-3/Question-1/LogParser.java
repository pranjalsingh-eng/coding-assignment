import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LogParser {

    private static final Set<String> VALID_LOG_TYPES =
            new HashSet<>(Arrays.asList("ERROR", "WARNING", "INFO", "DEBUG"));

    public static List<String> parseLogs(String filePath, int numLines, Set<String> logTypes)
            throws IOException {

        Path path = Paths.get(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "Invalid file path: \"" + filePath + "\". File does not exist or is not a regular file.");
        }

        for (String type : logTypes) {
            if (!VALID_LOG_TYPES.contains(type)) {
                throw new IllegalArgumentException(
                        "Invalid log type: \"" + type.toLowerCase()
                        + "\". Allowed types are: error, warning, info, debug.");
            }
        }

        List<String> allLines = Files.readAllLines(path);

        List<String> result = new ArrayList<>();
        for (int i = allLines.size() - 1; i >= 0 && result.size() < numLines; i--) {
            String line = allLines.get(i).trim();
            if (line.isEmpty()) continue;

            // Support both formats:
            // 1. "error ..." / "ERROR ..." (starts with keyword)
            // 2. "[ERROR] ..." (starts with bracketed keyword)
            String upper = line.toUpperCase();
            for (String type : logTypes) {
                if (upper.startsWith(type + " ") || upper.startsWith(type + ":") ||
                    upper.startsWith("[" + type + "]")) {
                    result.add(line);
                    break;
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: java LogParser <filePath> [numLines] [type1,type2,...]");
            System.err.println("  numLines  : optional, default 10");
            System.err.println("  types     : optional comma-separated list of error,warning,info,debug (default: error)");
            System.exit(1);
        }

        String filePath = args[0];

        int numLines = 10;
        if (args.length >= 2) {
            try {
                numLines = Integer.parseInt(args[1]);
                if (numLines < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("Error: numLines must be a positive integer. Got: \"" + args[1] + "\"");
                System.exit(1);
            }
        }

        Set<String> logTypes = new LinkedHashSet<>();
        if (args.length >= 3) {
            for (String t : args[2].split(",")) {
                logTypes.add(t.trim().toUpperCase());
            }
        } else {
            logTypes.add("ERROR");
        }

        try {
            List<String> results = parseLogs(filePath, numLines, logTypes);

            System.out.println("=".repeat(80));
            System.out.printf("File    : %s%n", filePath);
            System.out.printf("Types   : %s%n", String.join(", ", logTypes).toLowerCase());
            System.out.printf("Showing : up to %d most-recent matching line(s)%n", numLines);
            System.out.println("=".repeat(80));

            if (results.isEmpty()) {
                System.out.println("No matching log entries found.");
            } else {
                System.out.printf("Found %d matching log line(s):%n%n", results.size());
                for (int i = 0; i < results.size(); i++) {
                    System.out.printf("[%d] %s%n", i + 1, results.get(i));
                }
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
