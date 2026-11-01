package library.repository;

import java.util.List;
import java.util.Optional;

import library.model.Book;

public interface BookStorage {
    
    void save(Book book);   // store new or update books
    Optional<Book> findById(String Id);
    List<Book> findAll();
    void delete(String id);

}
