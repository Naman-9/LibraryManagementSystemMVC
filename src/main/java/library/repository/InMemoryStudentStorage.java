package library.repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import library.model.Student;

public class InMemoryStudentStorage implements StudentStorage {
    
    private final Map<String, Student> students;

    public InMemoryStudentStorage(){
        this.students = new HashMap<>();
    }

    public void save(Student student){
        students.put(student.getStudentId(), student);
    }

    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    public List<Student> findAll() {
        return students.values().stream().sorted(Comparator.comparing(Student::getStudentId)).toList();
    }

    public void delete(String id) {
        students.remove(id);
    }
    
}
