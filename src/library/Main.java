package library;

import library.controller.LibraryController;
import library.service.LibraryService;
import library.view.LibraryView;

public class Main {

    public static void main(String[] args) {
        LibraryService service = new LibraryService();
        service.loadDemoData();

        LibraryView view = new LibraryView();
        LibraryController controller = new LibraryController(service, view);

        controller.run();
    }
}
