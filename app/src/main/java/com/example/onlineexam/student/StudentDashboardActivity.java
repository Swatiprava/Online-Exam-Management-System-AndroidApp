package com.example.onlineexam.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.R;
import com.example.onlineexam.databinding.ActivityStudentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_purple));

        if (auth.getCurrentUser() != null) {
            loadStudentProfile();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.profileIconCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        binding.cardTakeExam.setOnClickListener(v ->
                startActivity(new Intent(this, ExamListActivity.class))
        );

        binding.cardMyResults.setOnClickListener(v ->
                startActivity(new Intent(this, MyResultsActivity.class))
        );

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) return true;
            else if (id == R.id.menu_exams) {
                startActivity(new Intent(this, ExamListActivity.class));
                return true;
            } else if (id == R.id.menu_results) {
                startActivity(new Intent(this, MyResultsActivity.class));
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadStudentProfile() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(this,
                                "User document not found!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String name = document.getString("name");
                    String branch = document.getString("branch");
                    String roll = document.getString("rollNo");

                    binding.tvWelcome.setText("Welcome, " + name);
                    binding.tvStudentInfo.setText(branch + " • Roll No: " + roll);

                    Log.d("TEST", "Name = " + name);

                    updateDashboardStats();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateDashboardStats() {

        String uid = auth.getCurrentUser().getUid();


        db.collection("results")
                .whereEqualTo("studentId", uid)
                .get()
                .addOnSuccessListener(resultSnapshot -> {

                    int examsDone = resultSnapshot.size();

                    int totalScore = 0;
                    int totalMarks = 0;

                    for (DocumentSnapshot doc : resultSnapshot.getDocuments()) {

                        Long score = doc.getLong("score");
                        Long marks = doc.getLong("totalMarks");

                        if (score != null)
                            totalScore += score.intValue();

                        if (marks != null)
                            totalMarks += marks.intValue();
                    }

                    int avgPercentage = 0;

                    if (totalMarks > 0) {
                        avgPercentage = (totalScore * 100) / totalMarks;
                    }

                    binding.tvExamsDoneCount.setText(String.valueOf(examsDone));
                    binding.tvAvgScorePercentage.setText(avgPercentage + "%");

                    loadUpcomingExams(examsDone);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void loadUpcomingExams(int examsDone) {

        db.collection("exams")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int totalExams = snapshot.size();

                    int upcoming = totalExams - examsDone;

                    if (upcoming < 0)
                        upcoming = 0;

                    binding.tvUpcomingCount.setText(String.valueOf(upcoming));

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            loadStudentProfile();
        }
    }
}