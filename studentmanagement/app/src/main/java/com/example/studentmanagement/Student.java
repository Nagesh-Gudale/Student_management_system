package com.example.studentmanagement;

import java.util.List;

public class Student {
    public String id;
    public String name;
    public String email;
    public String course;
    public String password;
    public List<String> completedCourses;

    // Required no-arg constructor for Firestore
    public Student() {}

    public Student(String id, String name, String email, String course, String password, List<String> completedCourses) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.course = course;
        this.password = password;
        this.completedCourses = completedCourses;
    }
}
