"""
Q2: Books of Ice and Fire
Tasks:
  a. Read list of books from API
  b. Create dictionary of {book_name: [pages, date_of_release, ISBN, publisher]}
  c. Create a CSV file with this dictionary
API URL: https://anapioficeandfire.com/api/books
"""

import requests
import csv
from datetime import datetime


API_URL = "https://anapioficeandfire.com/api/books"
OUTPUT_CSV = "books_output.csv"


def fetch_all_books(base_url: str) -> list[dict]:
    """Fetch all books from the API."""
    response = requests.get(base_url, timeout=15)
    response.raise_for_status()
    return response.json()


def format_date(raw_date: str) -> str:
    """Parse ISO date string and return formatted date."""
    try:
        dt = datetime.fromisoformat(raw_date.replace("Z", "+00:00"))
        return dt.strftime("%Y-%m-%d")
    except Exception:
        return raw_date


def build_books_dict(books: list[dict]) -> dict:
    """
    Build dictionary: {book_name: [pages, date_of_release, ISBN, publisher]}
    """
    books_dict = {}
    for book in books:
        name = book.get("name", "Unknown")
        pages = book.get("numberOfPages", 0)
        released = format_date(book.get("released", ""))
        isbn = book.get("isbn", "N/A")
        publisher = book.get("publisher", "N/A")
        books_dict[name] = [pages, released, isbn, publisher]
    return books_dict


def write_csv(books_dict: dict, filepath: str) -> None:
    """Write the books dictionary to a CSV file."""
    with open(filepath, "w", newline="", encoding="utf-8") as csvfile:
        writer = csv.writer(csvfile)
        # Header row
        writer.writerow(["Book Name", "Pages", "Date of Release", "ISBN", "Publisher"])
        for name, details in books_dict.items():
            writer.writerow([name] + details)


def main():
    print("=" * 60)
    print("Q2: Books of Ice and Fire")
    print("=" * 60)

    # a. Fetch books
    print("\n[a] Fetching books from API...")
    raw_books = fetch_all_books(API_URL)
    print(f"    Total books fetched: {len(raw_books)}")

    # b. Build dictionary
    print("\n[b] Building books dictionary...")
    books_dict = build_books_dict(raw_books)
    print("    Dictionary preview:")
    for name, details in books_dict.items():
        pages, released, isbn, publisher = details
        print(f"      '{name}': [Pages={pages}, Released={released}, ISBN={isbn}, Publisher={publisher}]")

    # c. Write CSV
    print(f"\n[c] Writing CSV to '{OUTPUT_CSV}'...")
    write_csv(books_dict, OUTPUT_CSV)
    print(f"    File written: {OUTPUT_CSV}")

    print(f"\nDone! {len(books_dict)} books saved to CSV.")
    return books_dict


if __name__ == "__main__":
    main()