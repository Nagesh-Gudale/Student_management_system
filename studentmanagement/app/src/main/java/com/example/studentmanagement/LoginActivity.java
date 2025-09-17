package com.example.studentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // First check in 'admins'
            db.collection("admins").document(username).get()
                    .addOnSuccessListener(adminSnapshot -> {
                        if (adminSnapshot.exists()) {
                            String adminPass = adminSnapshot.getString("password");
                            if (adminPass != null && adminPass.equals(password)) {
                                // Admin login success
                                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Not an admin, check 'students'
                            db.collection("students").document(username).get()
                                    .addOnSuccessListener(studentSnapshot -> {
                                        if (studentSnapshot.exists()) {
                                            String studentPass = studentSnapshot.getString("password");
                                            if (studentPass != null && studentPass.equals(password)) {
                                                // Student login success
                                                Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                                                intent.putExtra("studentId", username); // Pass the doc ID
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error checking student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
