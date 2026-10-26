package library.model;

import java.util.HashSet;
import java.util.Set;

public class Book {
    
    private final String id;              // final -> constructor cannot reassign
    private String title;
    private String author;
    private final Set<String> tags;  // tags reference cannot point to a different list
    private String issuedTo;
    private static int idCounter = 1;

    public static String generateId() {
        String initial = "B00";
        return initial+idCounter++;
    }

    public Book(String title, String author, Set<String> tags) {

        

        if(title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                "Book title cannot be blank."
            );
        }

        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(
                "Book author cannot be blank."
            );
        }

        

        this.id = generateId();
        this.title = title.trim();
        this.author = author.trim();
        this.tags = new HashSet<String>();
        if(tags != null){
            for (String tag : tags) {
                addTag(tag);
            }
        }
        this.issuedTo = null;
    }

    public String getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    public String getIssuedTo(){
        return issuedTo;
    }

    public boolean isAvailable(){
        return issuedTo == null;
    }

    public void updateTitle(String title) {
    if (title == null || title.isBlank()) {
        throw new IllegalArgumentException(
            "Book title cannot be blank."
        );
    }

    this.title = title.trim();
}

public void updateAuthor(String author) {
    if (author == null || author.isBlank()) {
        throw new IllegalArgumentException(
            "Book author cannot be blank."
        );
    }

    this.author = author.trim();
}

    public void addTag(String tag){
        if(tag == null || tag.isBlank()){
            throw new IllegalArgumentException(
                "Tag cannot be blank."
            );
        }
        
        tags.add(tag.trim().toLowerCase());
    }

    public boolean removeTag(String tag) {
    if(tag == null || tag.isBlank()){
            throw new IllegalArgumentException(
                "Tag cannot be blank."
            );
        }

    return tags.remove(tag.trim().toLowerCase());
}

    public void issueTo(String borrowerId) {
        if (borrowerId == null || borrowerId.isBlank()) {
            throw new IllegalArgumentException(
                "Borrower Id cannot be blank."
            );
        }

        if(!isAvailable()) {
            throw new IllegalStateException(
                "Book is already issued."
            );
        }

        issuedTo = borrowerId.trim();
    }



    public void returnBook() {
        if(isAvailable()){
            throw new IllegalStateException(
                "Book is not currently issued."
            );
        }
        issuedTo = null;
    }

    public boolean hasTag(String tag){
        if(tag == null || tag.isBlank()){
            return false;
        }
        return tags.contains(tag.trim().toLowerCase());
    }
}
