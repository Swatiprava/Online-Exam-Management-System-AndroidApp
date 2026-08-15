package com.example.onlineexam.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.databinding.ActivityCreateExamBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateExamActivity extends AppCompatActivity {

    private ActivityCreateExamBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<String> subjectNames = new ArrayList<>();
    private String selectedSubject = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("Create Exam");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadSubjects();

        binding.btnCreateExam.setOnClickListener(v -> {
            String title = binding.etExamTitle.getText().toString().trim();
            String duration = binding.etDuration.getText().toString().trim();
            String totalMarks = binding.etTotalMarks.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter exam title", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedSubject.isEmpty()) {
                Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }
            if (duration.isEmpty() || totalMarks.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnCreateExam.setEnabled(false);

            Map<String, Object> exam = new HashMap<>();
            exam.put("title", title);
            exam.put("subject", selectedSubject); // dropdown se aayega, dynamic subjects list se
            exam.put("duration", Integer.parseInt(duration));
            exam.put("totalMarks", Integer.parseInt(totalMarks));
            exam.put("createdBy", auth.getCurrentUser().getUid());
            exam.put("createdAt", System.currentTimeMillis());
            exam.put("isActive", true);

            db.collection("exams").add(exam)
                    .addOnSuccessListener(documentReference -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Exam Created Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCreateExam.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadSubjects() {
        db.collection("subjects").orderBy("name").get()
                .addOnSuccessListener(querySnapshot -> {
                    subjectNames.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (name != null) subjectNames.add(name);
                    }

                    if (subjectNames.isEmpty()) {
                        Toast.makeText(this,
                                "No subjects found. Add subjects first from 'Manage Subjects'.",
                                Toast.LENGTH_LONG).show();
                        binding.btnCreateExam.setEnabled(false);
                        return;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_dropdown_item, subjectNames);
                    binding.spinnerSubject.setAdapter(adapter);
                    selectedSubject = subjectNames.get(0);

                    binding.spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedSubject = subjectNames.get(position);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}