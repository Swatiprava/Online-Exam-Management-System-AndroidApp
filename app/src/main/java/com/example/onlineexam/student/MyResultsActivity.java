package com.example.onlineexam.student;

import android.content.SharedPreferences; // Added for persistence
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerResults;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvStudentNameHeader, tvStudentRollHeader;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Map<String, Object>> resultList;
    private ResultsAdapter adapter; // Named it ResultsAdapter as per your previous file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_results);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI Components Initialize
        recyclerResults = findViewById(R.id.recyclerResults);
        progressBar     = findViewById(R.id.progressBar);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvStudentNameHeader = findViewById(R.id.tvStudentName);
        tvStudentRollHeader = findViewById(R.id.tvRollNo);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        resultList = new ArrayList<>();
        // Using ResultsAdapter (Make sure the class name matches your file name)
        adapter    = new ResultsAdapter(resultList);

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);

        // First load from SharedPreferences for speed, then from Firestore for accuracy
        loadStudentDetailsFromPrefs();
        loadStudentDetailsFromFirestore();

        // Load Quiz Results
        loadResults();
    }

    private void loadStudentDetailsFromPrefs() {
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sp.getString("userName", "Student Name");
        String roll = sp.getString("userRoll", "Roll No: 00");

        if(tvStudentNameHeader != null) tvStudentNameHeader.setText(name);
        if(tvStudentRollHeader != null) tvStudentRollHeader.setText("Roll No: " + roll);
    }

    private void loadStudentDetailsFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String roll = document.getString("rollNo");

                        if(tvStudentNameHeader != null) tvStudentNameHeader.setText(name);
                        if(tvStudentRollHeader != null) tvStudentRollHeader.setText("Roll No: " + roll);

                        // Sync back to Prefs just in case
                        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                        editor.putString("userName", name);
                        editor.putString("userRoll", roll);
                        editor.apply();
                    }
                });
    }

    private void loadResults() {
        if (auth.getCurrentUser() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        String studentId = auth.getCurrentUser().getUid();

        // Fetching results specifically for this logged-in student
        db.collection("results")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    resultList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> result = new HashMap<>(doc.getData());
                        resultList.add(result);
                    }

                    adapter.notifyDataSetChanged();

                    if (resultList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No results yet. Give an exam first!");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}