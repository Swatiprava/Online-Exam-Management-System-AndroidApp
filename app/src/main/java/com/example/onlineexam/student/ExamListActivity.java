package com.example.onlineexam.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.onlineexam.databinding.ActivityExamListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamListActivity extends AppCompatActivity {

    private ActivityExamListBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> examList;
    private ExamListAdapter adapter; // Ensure ExamListAdapter is in the same package

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Exams");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        examList = new ArrayList<>();

        adapter = new ExamListAdapter(examList, exam -> {
            Intent intent = new Intent(this, TakeExamActivity.class);
            intent.putExtra("examId", (String) exam.get("docId"));
            intent.putExtra("examTitle", (String) exam.get("title"));
            intent.putExtra("duration", ((Long) exam.get("duration")).intValue());
            intent.putExtra("totalMarks", ((Long) exam.get("totalMarks")).intValue());
            startActivity(intent);
        });

        binding.recyclerExams.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerExams.setAdapter(adapter);

        loadExams();
    }

    private void loadExams() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("exams").whereEqualTo("isActive", true).get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    examList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> exam = new HashMap<>(doc.getData());
                        exam.put("docId", doc.getId());
                        examList.add(exam);
                    }
                    adapter.notifyDataSetChanged();
                    if (examList.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading exams", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}