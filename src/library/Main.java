package library;

import library.controller.LibraryController;
import library.repository.BookStorage;
import library.repository.FileBookStorage;
import library.repository.InMemoryBookStorage;
import library.service.LibraryService;
import library.view.LibraryView;

public class Main {

    public static void main(String[] args) {
        // BookStorage storage = new InMemoryBookStorage();
        BookStorage storage = new FileBookStorage();
        LibraryService service = new LibraryService(storage);

        // service.loadDemoData();

        LibraryView view = new LibraryView();
        LibraryController controller = new LibraryController(service, view);

        controller.run();
    }
}
