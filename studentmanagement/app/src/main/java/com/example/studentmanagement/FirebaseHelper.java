package com.example.studentmanagement;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference studentsRef = db.collection("students");

    public static void getAllStudents(EventListener listener) {
        studentsRef.addSnapshotListener(listener);
    }

    public static void getStudentById(String id, EventListener<DocumentSnapshot> listener) {
        studentsRef.document(id).addSnapshotListener(listener);
    }

    public static void updateStudent(String id, Student student) {
        studentsRef.document(id).set(student);
    }

    public static void deleteStudent(String id) {
        studentsRef.document(id).delete();
    }

    public static void addStudent(Student student) {
        studentsRef.document(student.id).set(student);
    }
}
