package library.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import library.model.Book;
import library.service.LibraryService;
import library.view.LibraryView;

public class LibraryController {

    private final LibraryService service;
    private final LibraryView view;

    public LibraryController(LibraryService service, LibraryView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        boolean running = true;

        while (running) {
            view.displayMenu();
            int choice = view.getUserChoice();

            switch (choice) {
                case 1 -> handleShowBooks();
                case 2 -> handleAddBook();
                case 3 -> handleUpdateBook();
                case 4 -> handleDeleteBook();
                case 5 -> handleIssueBook();
                case 6 -> handleReturnBook();
                case 7 -> showResults(service.searchById(view.getInput(("Enter Id: "))));
                case 8 -> showResults(service.searchByTitle(view.getInput("Enter Title: ")));
                case 9 -> showResults(service.searchByAuthor(view.getInput("Enter Author: ")));
                case 10 -> showResults(service.searchByTag(view.getInput("Enter Tag: ")));
                case 0 -> {
                    running = false;
                    view.displayMessage("Exit....");
                }
                default -> view.displayMessage("Invalid choice, try again.");
            }
        }
    }

    private void handleShowBooks() {
        view.displayBookList(service.getAllBooks());
    }

    private void handleAddBook() {
        String title = view.getInput("Title: ");
        String author = view.getInput("Author: ");
        String tagsInput = view.getInput("Tags (comma separated): ");

        Set<String> tags = new HashSet<>();
        for (String tag : tagsInput.split(",")) {
            if (!tag.isBlank()) {
                tags.add(tag.trim());
            }
        }

        try {
            service.addBook(title, author, tags);
            view.displayMessage("Book added.");
        } catch (IllegalArgumentException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleUpdateBook() {
        String id = view.getInput("Id: ");
        String title = view.getInput("Title: ");
        String author = view.getInput("Author: ");

        try {
            service.updateBook(id, title, author);
            view.displayMessage("Book updated.");
        } catch (IllegalArgumentException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleDeleteBook() {
        String id = view.getInput("Id to delete: ");

        try {
            service.deleteBook(id);
            view.displayMessage("Book deleted.");
        } catch (IllegalArgumentException e) {  
            view.displayMessage(e.getMessage());
        }
    }

    private void handleIssueBook() {
        String id = view.getInput("Id to issue: ");
        String borrowerId = view.getInput("Issue to: ");

        try {
            service.issueBook(id, borrowerId);
            view.displayMessage("Book issued.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleReturnBook() {
        String id = view.getInput("Id to return: ");

        try {
            service.returnBook(id);
            view.displayMessage("Book returned.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void showResults(List<Book> results){
        if(results.isEmpty()){
            view.displayMessage("No Book Found.");
        } else {
            view.displayBookList(results);
        }
    }
}