package com.example.onlineexam.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.LoginActivity;
import com.example.onlineexam.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding setup
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Status bar color change (optional but looks good)
        getWindow().setStatusBarColor(getResources().getColor(com.example.onlineexam.R.color.main_purple));

        // 1. Data load karo
        loadStudentProfileData();

        // 2. Logout functionality
        binding.btnLogoutProfile.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            // Saare purane pages clear karke login par le jaye
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditProfile.setOnClickListener(v -> {

            startActivity(new Intent(ProfileActivity.this,
                    EditProfileActivity.class));

        });

    }

    private void loadStudentProfileData() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String branch = document.getString("branch");
                        String roll = document.getString("rollNo");
                        String mobile = document.getString("phone");

                        // UI Update
                        binding.tvFullName.setText(name != null ? name : "Student Name");
                        binding.tvRollID.setText("ID: " + (roll != null ? roll : "N/A"));
                        binding.tvEmail.setText("📧  " + (email != null ? email : "Not set"));
                        binding.tvMobile.setText("📞  " + (mobile != null ? mobile : "Not set"));
                        binding.tvBranch.setText("🎓  " + (branch != null ? branch : "Not set"));

                        // Initials logic (e.g., Swatiprava -> S)
                        if (name != null && !name.isEmpty()) {
                            binding.tvInitials.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }

                        db.collection("results")
                                .whereEqualTo("studentId", uid)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    int examCount = snapshot.size();

                                    int totalScore = 0;
                                    int totalMarks = 0;

                                    for (var doc : snapshot.getDocuments()) {

                                        Long score = doc.getLong("score");
                                        Long marks = doc.getLong("totalMarks");

                                        if (score != null)
                                            totalScore += score.intValue();

                                        if (marks != null)
                                            totalMarks += marks.intValue();
                                    }

                                    binding.tvExamCount.setText(String.valueOf(examCount));

                                    int percentage = 0;

                                    if (totalMarks > 0)
                                        percentage = (totalScore * 100) / totalMarks;

                                    String grade;

                                    if (percentage >= 90)
                                        grade = "A+";
                                    else if (percentage >= 80)
                                        grade = "A";
                                    else if (percentage >= 70)
                                        grade = "B";
                                    else if (percentage >= 60)
                                        grade = "C";
                                    else
                                        grade = "F";

                                    binding.tvGrade.setText(grade);

                                    // Demo rank
                                    binding.tvRank.setText("#1");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadStudentProfileData();
    }
}