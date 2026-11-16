package library.view;

import java.util.List;
import java.util.Scanner;

import library.model.Book;
import library.model.BorrowingRecord;
import library.model.Student;

public class LibraryView {

    private final Scanner scanner = new Scanner(System.in);

    public String getInput(String input) {
        System.out.print(input);
        return scanner.nextLine();
    }

    public void displayTableHeader() {
        System.out.printf(
                "%-8s %-32s %-30s %-40s %-20s%n",
                "ID", "Title", "Author", "Tags", "Status");

        System.out.println("-".repeat(132));
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
        String status = book.isAvailable() ? "Available" : "Issued : " + book.getIssuedTo();

        System.out.printf(
                // "%-6s %-28s %-24s %-32s %-20s%n",
                "%-8s %-32s %-30s %-40s %-20s%n",
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                tags,
                status);

    }

    public void displayBookList(List<Book> books) {
        displayTableHeader();
        for (Book book : books) {
            displayBook(book);
        }
    }

    public void displayLoginMenu() {
        System.out.println("\n   LIBRARY MANAGEMENT SYSTEM  ");
        System.out.println("1. Admin Login");
        System.out.println("2. Student Login");
        System.out.println("0. Exit");
    }

    public void displayAdminMenu() {
        System.out.println("\n     ADMIN DASHBOARD    ");
        System.out.println("1. Show Books");
        System.out.println("2. Add Book");
        System.out.println("3. Update Book");
        System.out.println("4. Delete Book");
        System.out.println("5. Register Student");
        System.out.println("6. View All Students");
        System.out.println("7. View All Borrowing Records");
        System.out.println("0. Logout");
    }

    public void displayStudentMenu() {
        System.out.println("\n      STUDENT DASHBOARD     ");
        System.out.println("1. Show Books");
        System.out.println("2. Search Books");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. View My Borrowing History");
        System.out.println("0. Logout");
    }

    public void displayBorrowingRecords(List<BorrowingRecord> records) {

        System.out.printf("%-10s %-15s %-15s %-20s %-25s%n",
                "Record ID", "Book ID", "Student ID", "Issue Date", "Return Date");

        System.out
                .println("------------------------------------------------------------------------------------------");

        for (BorrowingRecord record : records) {

            String returnDate = record.getReturnDate() == null
                    ? "Not returned"
                    : record.getReturnDate().toString();

            System.out.printf("%-10s %-15s %-15s %-20s %-25s%n",
                    record.getRecordId(),
                    record.getBookId(),
                    record.getStudentId(),
                    record.getIssueDate(),
                    returnDate);
        }
    }

    public void displayStudentList(List<Student> students) {
        System.out.printf("%-15s %-25s%n", "Student ID", "Name");
        System.out.println("----------------------------------------");

        for (Student student : students) {
            System.out.printf("%-15s %-25s%n",
                    student.getStudentId(),
                    student.getName());

        }
    }

}
