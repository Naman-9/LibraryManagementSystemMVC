package library.repository;

import java.util.List;
import java.util.Optional;

import library.model.Student;

public interface StudentStorage {
    
    void save(Student student);
    Optional<Student> findById(String id);
    List<Student> findAll();
    void delete(String id);

}
