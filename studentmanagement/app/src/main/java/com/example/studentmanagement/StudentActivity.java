package com.example.studentmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity {
    TextView idView, nameView, emailView, courseView;
    ListView completedCoursesListView;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        FirebaseApp.initializeApp(this);

        idView = findViewById(R.id.idText);
        nameView = findViewById(R.id.nameText);
        emailView = findViewById(R.id.emailText);
        courseView = findViewById(R.id.courseText);
        completedCoursesListView = findViewById(R.id.completedCoursesListView); // Make sure this exists in layout

        db = FirebaseFirestore.getInstance();

        String studentId = getIntent().getStringExtra("studentId");

        db.collection("students").document(studentId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(StudentActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Student s = snapshot.toObject(Student.class);
                            if (s != null) {
                                idView.setText("ID: "+s.id);
                                nameView.setText("Name: "+s.name);
                                emailView.setText("Email: "+s.email);
                                courseView.setText("Course:  "+s.course);
                            }

                            List<String> courses = (List<String>) snapshot.get("completedCourses");
                            if (courses != null) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        StudentActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        new ArrayList<>(courses)
                                );
                                completedCoursesListView.setAdapter(adapter);
                            }
                        } else {
                            Toast.makeText(StudentActivity.this, "Student not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
