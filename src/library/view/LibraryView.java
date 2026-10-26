package library.view;

import java.util.List;
import java.util.Scanner;

import library.model.Book;

public class LibraryView {

    private final Scanner scanner = new Scanner(System.in);

    public String getInput(String input) {
        System.out.print(input);
        return scanner.nextLine();
    }

    public void displayTableHeader() {
        System.out.printf("%-10s %-25s %-20s %-25s %-20s%n",
                "ID", "Title", "Author", "Tags", "Status");
        System.out.println("-".repeat(105));
    }

    public void displayMenu() {
        System.out.println("\n--- Library Menu ---");
        System.out.println("1. Show Book List");
        System.out.println("2. Add Book");
        System.out.println("3. Update Book");
        System.out.println("4. Delete Book");
        System.out.println("5. Assign Book");
        System.out.println("6. Unassign Book ");
        System.out.println("7. Search by Id");
        System.out.println("8. Search by Title");
        System.out.println("9. Search by Author");
        System.out.println("10. Search by Tag");
        System.out.println("0. Exit");
        System.out.println();
    }

    public int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Please enter a valid number.");
            return -1;
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayBook(Book book) {

        String tags = book.getTags().isEmpty() ? "None" : String.join(",", book.getTags());
        String status = book.isAvailable() ? "Available" : "Issued to " + book.getIssuedTo();

        System.out.printf("%-10s %-25s %-20s %-25s %-20s%n",
                book.getId(), book.getTitle(), book.getAuthor(), tags, status);

    }

    public void displayBookList(List<Book> books) {
        displayTableHeader();
        for (Book book : books) {
            displayBook(book);
        }
    }

}
