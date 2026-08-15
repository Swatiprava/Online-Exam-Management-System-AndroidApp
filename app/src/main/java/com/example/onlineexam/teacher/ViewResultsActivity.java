package com.example.onlineexam.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.onlineexam.student.ResultsAdapter; // Check name here
import com.example.onlineexam.databinding.ActivityViewResultsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewResultsActivity extends AppCompatActivity {

    private ActivityViewResultsBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> resultList;
    private ResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Student Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        resultList = new ArrayList<>();

        // Final Adapter call
        adapter = new ResultsAdapter(resultList);
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(adapter);

        loadResults();
    }

    private void loadResults() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("results").get()
                .addOnSuccessListener(querySnapshot -> {

                    resultList.clear();

                    List<Map<String, Object>> rawResults = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> result = new HashMap<>(doc.getData());
                        rawResults.add(result);
                    }

                    if (rawResults.isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Har result ke liye "users" collection se current naam/rollNo
                    // live fetch karo (join). AtomicInteger se track karte hain
                    // ki sab async calls complete hue ya nahi.
                    AtomicInteger pending = new AtomicInteger(rawResults.size());

                    for (Map<String, Object> result : rawResults) {
                        String studentId = (String) result.get("studentId");

                        if (studentId == null) {
                            // Purana data jisme studentId hi nahi hai - "Unknown" dikhado
                            result.put("studentName", "Unknown Student");
                            result.put("rollNo", "N/A");
                            resultList.add(result);
                            checkIfDone(pending);
                            continue;
                        }

                        db.collection("users").document(studentId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        result.put("studentName", userDoc.getString("name"));
                                        result.put("rollNo", userDoc.getString("rollNo"));
                                    } else {
                                        // User delete ho chuka ho ya na mile
                                        result.put("studentName", "Deleted User");
                                        result.put("rollNo", "N/A");
                                    }
                                    resultList.add(result);
                                    checkIfDone(pending);
                                })
                                .addOnFailureListener(e -> {
                                    result.put("studentName", "Error loading name");
                                    result.put("rollNo", "N/A");
                                    resultList.add(result);
                                    checkIfDone(pending);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfDone(AtomicInteger pending) {
        if (pending.decrementAndGet() == 0) {
            binding.progressBar.setVisibility(View.GONE);
            if (resultList.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}