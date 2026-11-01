package library.model;

public class Student {
    
    private final String studentId;
    private String name;
    private String password;
    private static int idCounter = 1;

    private static String generateId() {
        String initial = "S00";
        return initial + idCounter++;
    }

    public Student(String name, String password){

        if(name == null || name.isBlank()){
            throw new IllegalArgumentException(
                "Please provide name."
            );
        }
        if(password == null || password.isBlank()){
            throw new IllegalArgumentException(
                "Please provide password."
            );
        }

        this.studentId = generateId();
        this.name = name.trim();
        this.password = password;
    }

    public String getStudentId() {
        return studentId;
    }
    public String getName() {
        return name;
    }
    public boolean verifyPassword (String password) {
        return this.password.equals(password);
    }

    

}
