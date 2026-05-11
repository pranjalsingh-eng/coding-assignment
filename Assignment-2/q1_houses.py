"""
Q1: Houses of Ice and Fire
Tasks:
  a. Create a list of all houses and regions from API
  b. Write this list in a text file
  c. Order all houses alphabetically
API URL: https://anapioficeandfire.com/api/houses
"""

import requests
from collections import Counter

API_URL     = "https://anapioficeandfire.com/api/houses"
OUTPUT_FILE = "houses_output.txt"


# ── Print helpers ──────────────────────────────────────────────────────────

def banner(text):
    w = 62
    print("\n" + "═" * w)
    print("║  " + text.ljust(w - 4) + "║")
    print("═" * w)

def step(label, text):
    print(f"  {label}  {text}")

def ok(text):
    print(f"  ✔  {text}")

def info(text):
    print(f"  ℹ  {text}")

def thin(char="─", width=58):
    print("  " + char * width)

def section(text):
    print(f"\n  ━━━  {text}  ━━━")


# ── API fetch ──────────────────────────────────────────────────────────────

def fetch_all_houses(base_url):
    """Fetch all houses from the API with pagination."""
    houses    = []
    page      = 1
    page_size = 50

    info("Starting paginated fetch from API...")
    thin()

    while True:
        params   = {"page": page, "pageSize": page_size}
        response = requests.get(base_url, params=params, timeout=15)
        response.raise_for_status()

        data = response.json()
        if not data:
            break

        houses.extend(data)
        print(f"    Page {page:>2}  ->  {len(data):>2} houses  (running total: {len(houses)})")

        if len(data) < page_size:
            break
        page += 1

    thin()
    return houses


def extract_house_region(house):
    """Return (name, region) for a house entry."""
    return house.get("name", "Unknown"), house.get("region", "Unknown Region")


# ── File writer ────────────────────────────────────────────────────────────

def write_to_file(house_region_list):
    """Write sorted house list to a formatted text file."""
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write("Houses of Ice and Fire - Sorted Alphabetically\n")
        f.write("=" * 70 + "\n\n")
        f.write(f"{'#':<6}{'House Name':<45}{'Region'}\n")
        f.write("-" * 80 + "\n")
        for idx, (name, region) in enumerate(house_region_list, start=1):
            f.write(f"{idx:<6}{name:<45}{region}\n")


# ── Terminal preview ───────────────────────────────────────────────────────

def print_preview(house_region_list, preview=10):
    """Print a formatted preview table in the terminal."""
    print()
    print(f"  {'#':<6}{'House Name':<45}{'Region'}")
    thin("─", 71)
    for idx, (name, region) in enumerate(house_region_list[:preview], start=1):
        print(f"  {idx:<6}{name:<45}{region}")
    remaining = len(house_region_list) - preview
    if remaining > 0:
        print(f"    ... and {remaining} more houses (see {OUTPUT_FILE})")
    thin("─", 71)


# ── Region bar chart ───────────────────────────────────────────────────────

def print_region_summary(house_region_list):
    """Print house count per region with a bar chart."""
    counts = Counter(region for _, region in house_region_list)
    print()
    print(f"  {'Region':<35}{'Count':>5}  Bar")
    thin("─", 58)
    for region, count in sorted(counts.items(), key=lambda x: -x[1]):
        bar = "█" * min(count, 25)
        print(f"  {region:<35}{count:>5}  {bar}")
    thin("─", 58)


# ── Main ───────────────────────────────────────────────────────────────────

def main():
    # Header banner
    banner("Q1 - Houses of Ice and Fire")

    # [a] Fetch
    print()
    step("[a]", "Fetching all houses from the API...")
    print()
    raw_houses = fetch_all_houses(API_URL)
    ok(f"Total houses fetched: {len(raw_houses)}")

    house_region_list = [extract_house_region(h) for h in raw_houses]

    # [c] Sort
    print()
    step("[c]", "Sorting houses alphabetically by name...")
    house_region_list.sort(key=lambda x: x[0].lower())
    ok("Houses sorted successfully.")

    # [b] Write file
    print()
    step("[b]", f"Writing results to '{OUTPUT_FILE}'...")
    write_to_file(house_region_list)
    ok(f"File written  ->  {OUTPUT_FILE}")

    # Preview table
    section("Preview: First 10 Houses (A-Z)")
    print_preview(house_region_list, preview=10)

    # Region chart
    section("Houses by Region")
    print_region_summary(house_region_list)

    # Done
    print()
    thin()
    ok(f"Q1 Complete!  {len(house_region_list)} houses saved to {OUTPUT_FILE}")
    thin()
    print()

    return house_region_list


if __name__ == "__main__":
    main()