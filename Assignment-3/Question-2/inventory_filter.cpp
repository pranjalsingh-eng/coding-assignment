/**
 * inventory_filter.cpp
 *
 * Filters inventory JSON data based on:
 *   - "memory"  → machine with the highest RAM
 *   - "cpu"     → machine with the highest CPU speed
 *   - "linux"   → all Linux machines
 *   - "windows" → all Windows machines
 *
 * Design:
 *   InventoryItem   – value object for one machine record
 *   InventoryParser – loads & owns all records (streaming-friendly)
 *   FilterStrategy  – abstract base for the filter pattern
 *   MaxMemoryFilter / MaxCpuFilter / OsFilter – concrete strategies
 *   InventoryFilter – context that runs any FilterStrategy
 *
 * Build:
 *   g++ -std=c++17 -O2 -o inventory_filter inventory_filter.cpp
 *
 * Usage:
 *   ./inventory_filter <path/to/inventory.json> <filter_criteria>
 *
 * Examples:
 *   ./inventory_filter inventory.json memory
 *   ./inventory_filter inventory.json cpu
 *   ./inventory_filter inventory.json linux
 *   ./inventory_filter inventory.json windows
 */

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <memory>
#include <stdexcept>
#include <algorithm>
#include <cctype>
#include <filesystem>

// ─────────────────────────────────────────────────────────────────────────────
// InventoryItem  –  plain value object
// ─────────────────────────────────────────────────────────────────────────────
struct InventoryItem {
    std::string ip;
    std::string os;
    std::string memory;   // raw string, e.g. "16GB"
    std::string cpu;      // raw string, e.g. "3.8Ghz"
    std::string disk;

    // Returns the numeric part of memory ("16GB" → 16.0)
    double memoryValue() const {
        return extractNumeric(memory);
    }

    // Returns the numeric part of CPU speed ("3.8Ghz" → 3.8)
    double cpuValue() const {
        return extractNumeric(cpu);
    }

    void print() const {
        std::cout << "  IP     : " << ip     << "\n"
                  << "  OS     : " << os     << "\n"
                  << "  Memory : " << memory << "\n"
                  << "  CPU    : " << cpu    << "\n"
                  << "  Disk   : " << disk   << "\n";
    }

private:
    static double extractNumeric(const std::string& s) {
        // Walk the string, keep digits and the first dot
        std::string num;
        bool hasDot = false;
        for (char c : s) {
            if (std::isdigit(static_cast<unsigned char>(c))) {
                num += c;
            } else if (c == '.' && !hasDot) {
                num += c;
                hasDot = true;
            }
        }
        if (num.empty()) return 0.0;
        try { return std::stod(num); }
        catch (...) { return 0.0; }
    }
};

// ─────────────────────────────────────────────────────────────────────────────
// Minimal JSON parser  –  no external library; optimised for large files
//
// Strategy: single-pass, character-by-character read.
// We only care about the "inventory" object's children, each of which
// is a flat key/value object.  We never build an in-memory DOM.
// ─────────────────────────────────────────────────────────────────────────────
class InventoryParser {
public:
    explicit InventoryParser(const std::string& filePath) {
        // CWE-22 + CWE-862: resolve canonical path and restrict to CWD
        namespace fs = std::filesystem;
        fs::path canonical;
        try {
            canonical = fs::canonical(fs::path(filePath));
        } catch (...) {
            throw std::runtime_error("Invalid or inaccessible file path.");
        }
        fs::path base = fs::canonical(fs::current_path());
        // CWE-862: only allow files within the current working directory
        std::string canonStr = canonical.string();
        std::string baseStr  = base.string();
        // Ensure baseStr ends with a separator to avoid prefix false-matches
        if (!baseStr.empty() && baseStr.back() != '\\' && baseStr.back() != '/')
            baseStr += std::filesystem::path::preferred_separator;
        if (canonStr.rfind(baseStr, 0) != 0)
            throw std::runtime_error("Access denied: file is outside the allowed directory.");

        std::ifstream file(canonical);
        if (!file.is_open())
            throw std::runtime_error("Cannot open the specified inventory file.");

        // Read the whole file into a string buffer (one syscall, fast for big files)
        std::ostringstream buf;
        buf << file.rdbuf();
        raw_ = buf.str();
        pos_ = 0;
        parse();
    }

    const std::vector<InventoryItem>& items() const { return items_; }

private:
    std::string raw_;
    size_t      pos_;
    std::vector<InventoryItem> items_;

    // ── Helpers ──────────────────────────────────────────────────────────────
    void skipWhitespace() {
        while (pos_ < raw_.size() && std::isspace(static_cast<unsigned char>(raw_[pos_])))
            ++pos_;
    }

    // Consume expected char or throw
    // CWE-480: use explicit unsigned char cast for comparison
    // CWE-117: error message contains no user-tainted data
    void expect(char c) {
        skipWhitespace();
        bool mismatch = (pos_ >= raw_.size()) ||
                        (static_cast<unsigned char>(raw_[pos_]) != static_cast<unsigned char>(c));
        if (mismatch)
            throw std::runtime_error("JSON parse error: unexpected character.");
        ++pos_;
    }

    // Fix CWE-480: proper escape handling with full bounds checks
    std::string parseString() {
        expect('"');
        std::string result;
        while (pos_ < raw_.size()) {
            char c = raw_[pos_++];
            if (c == '\\') {
                // Fix CWE-480: check bounds before reading escape char
                if (pos_ >= raw_.size()) break;
                char esc = raw_[pos_++];
                switch (esc) {
                    case '"':  result += '"';  break;
                    case '\\': result += '\\'; break;
                    case '/':  result += '/';  break;
                    case 'n':  result += '\n'; break;
                    case 'r':  result += '\r'; break;
                    case 't':  result += '\t'; break;
                    default:   result += esc;  break;
                }
            } else if (c == '"') {
                break;
            } else {
                result += c;
            }
        }
        return result;
    }

    // ── Top-level parse ───────────────────────────────────────────────────────
    void parse() {
        expect('{');
        while (pos_ < raw_.size()) {
            skipWhitespace();
            // Fix CWE-480: bounds check before indexing raw_
            if (pos_ < raw_.size() && raw_[pos_] == '}') break;

            std::string key = parseString();
            expect(':');

            if (key == "inventory") {
                parseInventoryObject();
            } else {
                skipValue();  // unknown top-level key
            }

            skipWhitespace();
            if (pos_ < raw_.size() && raw_[pos_] == ',') ++pos_;
        }
    }

    // Parse the object whose keys are IP addresses
    void parseInventoryObject() {
        expect('{');
        while (pos_ < raw_.size()) {
            skipWhitespace();
            // Fix CWE-480: bounds check before indexing raw_
            if (pos_ < raw_.size() && raw_[pos_] == '}') { ++pos_; break; }

            parseString();  // IP key (also stored inside the object as "ip")
            expect(':');
            items_.push_back(parseMachineObject());

            skipWhitespace();
            if (pos_ < raw_.size() && raw_[pos_] == ',') ++pos_;
        }
    }

    // Parse one machine object { "ip":..., "os":..., ... }
    InventoryItem parseMachineObject() {
        InventoryItem item;
        expect('{');
        while (pos_ < raw_.size()) {
            skipWhitespace();
            // Fix CWE-480: bounds check before indexing raw_
            if (pos_ < raw_.size() && raw_[pos_] == '}') { ++pos_; break; }

            std::string k = parseString();
            expect(':');
            std::string v = parseString();

            if      (k == "ip")     item.ip     = v;
            else if (k == "os")     item.os     = v;
            else if (k == "memory") item.memory = v;
            else if (k == "cpu")    item.cpu    = v;
            else if (k == "disk")   item.disk   = v;

            skipWhitespace();
            if (pos_ < raw_.size() && raw_[pos_] == ',') ++pos_;
        }
        return item;
    }

    // Skip any JSON value (object, array, string, number, literal)
    void skipValue() {
        skipWhitespace();
        if (pos_ >= raw_.size()) return;
        char c = raw_[pos_];
        if (c == '"') {
            parseString();
        } else if (c == '{') {
            ++pos_;
            int depth = 1;
            while (pos_ < raw_.size() && depth > 0) {
                char ch = raw_[pos_++];
                if (ch == '{') ++depth;
                else if (ch == '}') --depth;
                else if (ch == '"') { --pos_; parseString(); }
            }
        } else if (c == '[') {
            ++pos_;
            int depth = 1;
            while (pos_ < raw_.size() && depth > 0) {
                char ch = raw_[pos_++];
                if (ch == '[') ++depth;
                else if (ch == ']') --depth;
                else if (ch == '"') { --pos_; parseString(); }
            }
        } else {
            // number / true / false / null
            while (pos_ < raw_.size()
                   && raw_[pos_] != ',' && raw_[pos_] != '}'
                   && raw_[pos_] != ']' && !std::isspace((unsigned char)raw_[pos_]))
                ++pos_;
        }
    }
};

// ─────────────────────────────────────────────────────────────────────────────
// Filter Strategy hierarchy
// ─────────────────────────────────────────────────────────────────────────────
class FilterStrategy {
public:
    virtual ~FilterStrategy() = default;
    virtual std::vector<InventoryItem> apply(
        const std::vector<InventoryItem>& items) const = 0;
    virtual std::string description() const = 0;
};

// Returns the single item with the highest memory value
class MaxMemoryFilter : public FilterStrategy {
public:
    std::vector<InventoryItem> apply(
        const std::vector<InventoryItem>& items) const override
    {
        if (items.empty()) return {};
        auto it = std::max_element(items.begin(), items.end(),
            [](const InventoryItem& a, const InventoryItem& b){
                return a.memoryValue() < b.memoryValue();
            });
        return { *it };
    }
    std::string description() const override {
        return "Machine with maximum Memory";
    }
};

// Returns the single item with the highest CPU speed
class MaxCpuFilter : public FilterStrategy {
public:
    std::vector<InventoryItem> apply(
        const std::vector<InventoryItem>& items) const override
    {
        if (items.empty()) return {};
        auto it = std::max_element(items.begin(), items.end(),
            [](const InventoryItem& a, const InventoryItem& b){
                return a.cpuValue() < b.cpuValue();
            });
        return { *it };
    }
    std::string description() const override {
        return "Machine with maximum CPU speed";
    }
};

// Returns all items whose OS matches (case-insensitive)
class OsFilter : public FilterStrategy {
public:
    explicit OsFilter(const std::string& osType) : osType_(osType) {}

    std::vector<InventoryItem> apply(
        const std::vector<InventoryItem>& items) const override
    {
        std::vector<InventoryItem> result;
        for (const auto& item : items) {
            if (toLower(item.os) == toLower(osType_))
                result.push_back(item);
        }
        return result;
    }
    std::string description() const override {
        return "Machines with OS = " + osType_;
    }

private:
    std::string osType_;
    static std::string toLower(std::string s) {
        std::transform(s.begin(), s.end(), s.begin(),
            [](unsigned char c){ return std::tolower(c); });
        return s;
    }
};

// ─────────────────────────────────────────────────────────────────────────────
// InventoryFilter  –  context that runs a strategy
// ─────────────────────────────────────────────────────────────────────────────
class InventoryFilter {
public:
    explicit InventoryFilter(std::unique_ptr<FilterStrategy> strategy)
        : strategy_(std::move(strategy)) {}

    void run(const std::vector<InventoryItem>& items) const {
        std::cout << "\nFilter : " << strategy_->description() << "\n";
        std::cout << std::string(50, '-') << "\n";

        auto results = strategy_->apply(items);

        if (results.empty()) {
            std::cout << "No matching inventory items found.\n";
            return;
        }
        std::cout << "Result (" << results.size() << " record(s)):\n\n";
        for (size_t i = 0; i < results.size(); ++i) {
            std::cout << "  [" << (i + 1) << "]\n";
            results[i].print();
            std::cout << "\n";
        }
    }

private:
    std::unique_ptr<FilterStrategy> strategy_;
};

// ─────────────────────────────────────────────────────────────────────────────
// Factory: builds the right strategy from the CLI argument
// ─────────────────────────────────────────────────────────────────────────────
std::unique_ptr<FilterStrategy> makeStrategy(const std::string& criteria) {
    // Normalise to lower-case
    std::string lc = criteria;
    std::transform(lc.begin(), lc.end(), lc.begin(),
        [](unsigned char c){ return std::tolower(c); });

    if (lc == "memory")  return std::make_unique<MaxMemoryFilter>();
    if (lc == "cpu")     return std::make_unique<MaxCpuFilter>();
    if (lc == "linux")   return std::make_unique<OsFilter>("Linux");
    if (lc == "windows") return std::make_unique<OsFilter>("Windows");

    throw std::invalid_argument(
        "Invalid filter criteria: \"" + criteria + "\". "
        "Allowed values: memory, cpu, linux, windows.");
}

// ─────────────────────────────────────────────────────────────────────────────
// main
// ─────────────────────────────────────────────────────────────────────────────
int main(int argc, char* argv[]) {

    // ── Argument validation ──────────────────────────────────────────────────
    if (argc < 3) {
        // Fix CWE-117: no user input in this message, safe to print directly
        std::cerr << "Usage: inventory_filter <inventory.json> <filter_criteria>\n"
                  << "  filter_criteria: memory | cpu | linux | windows\n";
        return 1;
    }

    std::string filePath = argv[1];
    std::string criteria = argv[2];

    // Fix CWE-117: sanitize inputs before any output — strip control characters
    auto sanitize = [](const std::string& s) {
        std::string out;
        out.reserve(s.size());
        for (unsigned char c : s)
            if (c >= 0x20 && c != 0x7F) out += static_cast<char>(c);
        return out;
    };
    std::string safeFilePath = sanitize(filePath);
    std::string safeCriteria = sanitize(criteria);

    try {
        // ── Load inventory ───────────────────────────────────────────────────
        InventoryParser parser(filePath);
        const auto& items = parser.items();

        // Fix CWE-117: use sanitized path in output
        std::cout << "Loaded " << items.size()
                  << " inventory record(s) from: " << safeFilePath << "\n";

        auto strategy = makeStrategy(safeCriteria);
        InventoryFilter filter(std::move(strategy));
        filter.run(items);

    } catch (const std::invalid_argument& e) {
        // CWE-117: sanitize exception message before printing
        std::string msg = e.what();
        std::string safeMsg;
        for (unsigned char ch : msg)
            if (ch >= 0x20 && ch != 0x7F) safeMsg += static_cast<char>(ch);
        std::cerr << "Validation Error: " << safeMsg << "\n";
        return 1;
    } catch (const std::runtime_error& e) {
        // CWE-117: sanitize exception message before printing
        std::string msg = e.what();
        std::string safeMsg;
        for (unsigned char ch : msg)
            if (ch >= 0x20 && ch != 0x7F) safeMsg += static_cast<char>(ch);
        std::cerr << "Runtime Error: " << safeMsg << "\n";
        return 1;
    } catch (...) {
        std::cerr << "Unknown error occurred.\n";
        return 1;
    }

    return 0;
}