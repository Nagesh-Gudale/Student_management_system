package com.example.studentmanagement;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.*;

import java.util.*;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView studentRecyclerView;
    private FirebaseFirestore db;
    private ArrayList<Student> studentList = new ArrayList<>();
    private StudentAdapter adapter;
    private Button addStudentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        studentRecyclerView = findViewById(R.id.studentRecyclerView);
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(this, studentList);
        studentRecyclerView.setAdapter(adapter);

        addStudentBtn = findViewById(R.id.addStudentBtn);
        addStudentBtn.setOnClickListener(v -> showAddStudentDialog());

        loadStudentsFromFirestore();
    }

    private void loadStudentsFromFirestore() {
        db.collection("students").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                return;
            }

            studentList.clear();
            for (DocumentSnapshot doc : snapshots) {
                Student student = doc.toObject(Student.class);
                if (student != null) studentList.add(student);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Add New Student");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText idInput = new EditText(this);
        idInput.setHint("🆔 ID");
        layout.addView(idInput);

        EditText nameInput = new EditText(this);
        nameInput.setHint("👤 Name");
        layout.addView(nameInput);

        EditText emailInput = new EditText(this);
        emailInput.setHint("📧 Email");
        layout.addView(emailInput);

        EditText courseInput = new EditText(this);
        courseInput.setHint("📚 Course");
        layout.addView(courseInput);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("🔐 Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            Student newStudent = new Student(
                    idInput.getText().toString(),
                    nameInput.getText().toString(),
                    emailInput.getText().toString(),
                    courseInput.getText().toString(),
                    passwordInput.getText().toString(),
                    new ArrayList<>()
            );

            db.collection("students").document(newStudent.id)
                    .set(newStudent)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Student added!", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ================= Adapter =================

    class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private Context context;
        private ArrayList<Student> students;

        StudentAdapter(Context context, ArrayList<Student> students) {
            this.context = context;
            this.students = students;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(context);
            cardView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            cardView.setRadius(24f);
            cardView.setCardElevation(8f);
            cardView.setUseCompatPadding(true);
            cardView.setContentPadding(32, 32, 32, 32);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            cardView.addView(layout);

            return new StudentViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            holder.bind(students.get(position));
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        class StudentViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            TextView info;
            LinearLayout buttonLayout;

            StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                layout = (LinearLayout) ((CardView) itemView).getChildAt(0);

                info = new TextView(context);
                info.setTextSize(16f);
                info.setTextColor(0xFF333333);
                info.setPadding(0, 0, 0, 16);
                layout.addView(info);

                buttonLayout = new LinearLayout(context);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.END);
                layout.addView(buttonLayout);
            }

            void bind(Student student) {
                info.setText("🆔 ID: " + student.id +
                        "\n👤 Name: " + student.name +
                        "\n📧 Email: " + student.email +
                        "\n📚 Course: " + student.course +
                        "\n🔐 Password: " + student.password);

                layout.removeViews(1, layout.getChildCount() - 2); // Keep info and buttons

                if (student.completedCourses != null && !student.completedCourses.isEmpty()) {
                    TextView label = new TextView(context);
                    label.setText("✅ Completed Courses:");
                    label.setTypeface(null, Typeface.BOLD);
                    label.setPadding(0, 16, 0, 8);
                    layout.addView(label, layout.getChildCount() - 1);

                    for (String courseName : student.completedCourses) {
                        TextView courseItem = new TextView(context);
                        courseItem.setText("🎓 " + courseName);
                        courseItem.setPadding(16, 4, 0, 4);
                        layout.addView(courseItem, layout.getChildCount() - 1);
                    }
                }

                buttonLayout.removeAllViews();

                Button viewBtn = new Button(context);
                viewBtn.setText("View");
                viewBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminActivity.this, StudentActivity.class);
                    intent.putExtra("studentId", student.id);
                    context.startActivity(intent);
                });
                buttonLayout.addView(viewBtn);

                Button editBtn = new Button(context);
                editBtn.setText("Edit");
                editBtn.setOnClickListener(v -> showEditDialog(student));
                buttonLayout.addView(editBtn);

                Button deleteBtn = new Button(context);
                deleteBtn.setText("Delete");
                deleteBtn.setOnClickListener(v -> {
                    db.collection("students").document(student.id).delete();
                    Toast.makeText(context, "Deleted " + student.name, Toast.LENGTH_SHORT).show();
                });
                buttonLayout.addView(deleteBtn);
            }

            void showEditDialog(Student student) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("✏️ Edit Student");

                ScrollView scrollView = new ScrollView(context);
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 10);
                scrollView.addView(layout);

                EditText nameInput = new EditText(context);
                nameInput.setHint("👤 Name");
                nameInput.setText(student.name);
                layout.addView(nameInput);

                EditText emailInput = new EditText(context);
                emailInput.setHint("📧 Email");
                emailInput.setText(student.email);
                layout.addView(emailInput);

                EditText courseInput = new EditText(context);
                courseInput.setHint("📚 Course");
                courseInput.setText(student.course);
                layout.addView(courseInput);

                EditText passwordInput = new EditText(context);
                passwordInput.setHint("🔐 Password");
                passwordInput.setText(student.password);
                layout.addView(passwordInput);

                // Completed courses label
                TextView label = new TextView(context);
                label.setText("✅ Completed Courses:");
                label.setTypeface(null, Typeface.BOLD);
                label.setPadding(0, 20, 0, 10);
                layout.addView(label);

                LinearLayout courseListLayout = new LinearLayout(context);
                courseListLayout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(courseListLayout);

                // Pre-fill existing courses
                if (student.completedCourses != null) {
                    for (String course : student.completedCourses) {
                        addCourseField(courseListLayout, course);
                    }
                }

                Button addCourseBtn = new Button(context);
                addCourseBtn.setText("➕ Add Course");
                addCourseBtn.setOnClickListener(v -> addCourseField(courseListLayout, ""));
                layout.addView(addCourseBtn);

                builder.setView(scrollView);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    Map<String, Object> updated = new HashMap<>();
                    updated.put("name", nameInput.getText().toString());
                    updated.put("email", emailInput.getText().toString());
                    updated.put("course", courseInput.getText().toString());
                    updated.put("password", passwordInput.getText().toString());

                    List<String> completedCourses = new ArrayList<>();
                    for (int i = 0; i < courseListLayout.getChildCount(); i++) {
                        EditText courseField = (EditText) courseListLayout.getChildAt(i);
                        String course = courseField.getText().toString().trim();
                        if (!course.isEmpty()) completedCourses.add(course);
                    }
                    updated.put("completedCourses", completedCourses);

                    db.collection("students").document(student.id)
                            .update(updated)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show());
                });

                builder.setNegativeButton("Cancel", null);
                builder.show();
            }

            // Helper method to add a single course input field
            void addCourseField(LinearLayout parent, String courseName) {
                EditText courseField = new EditText(context);
                courseField.setHint("Course name");
                courseField.setText(courseName);
                parent.addView(courseField);
            }

        }
    }
}
