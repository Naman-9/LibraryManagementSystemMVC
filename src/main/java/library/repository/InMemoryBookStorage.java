package library.repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import library.model.Book;

public class InMemoryBookStorage implements BookStorage {
    
    private final Map<String, Book> books;

    public InMemoryBookStorage(){
        this.books = new HashMap<>();
    }

    public void save(Book book){
        books.put(book.getId(), book);
    }

    public Optional<Book> findById(String id){
        return Optional.ofNullable(books.get(id));
    }

    public List<Book> findAll(){
        // return new ArrayList<Book>(books.values());
        return books.values().stream().sorted(Comparator.comparing(Book::getId)).toList();
    }

    public void delete(String id){
        books.remove(id);
    }
}
