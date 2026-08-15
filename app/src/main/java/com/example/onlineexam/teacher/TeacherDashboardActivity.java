package com.example.onlineexam.teacher;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.LoginActivity;
import com.example.onlineexam.databinding.ActivityTeacherDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    private ActivityTeacherDashboardBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        loadTeacherName();

        // 1. ADD QUESTIONS
        binding.cardAddQuestions.setOnClickListener(v ->
                startActivity(new Intent(this, AddQuestionsActivity.class))
        );

        // 2. MANAGE QUESTIONS (Delete/Update)
        binding.cardManageQuestions.setOnClickListener(v ->
                startActivity(new Intent(this, ManageQuestionsActivity.class))
        );

        // 3. VIEW RESULTS
        binding.cardViewResults.setOnClickListener(v ->
                startActivity(new Intent(this, ViewResultsActivity.class))
        );

        // LOGOUT
        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadTeacherName() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            binding.tvWelcome.setText("Welcome, " + name);
                        }
                    });
        }
    }
}