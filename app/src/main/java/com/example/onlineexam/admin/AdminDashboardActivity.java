package com.example.onlineexam.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.LoginActivity;
import com.example.onlineexam.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadAdminName();

        binding.cardCreateExam.setOnClickListener(v ->
                startActivity(new Intent(this, CreateExamActivity.class))
        );

        binding.cardManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, ManageUsersActivity.class))
        );

        binding.cardViewResults.setOnClickListener(v ->
                startActivity(new Intent(this, AdminViewResultsActivity.class))
        );

        // NAYA: Manage Subjects card click listener
        binding.cardManageSubjects.setOnClickListener(v ->
                startActivity(new Intent(this, ManageSubjectsActivity.class))
        );

        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadAdminName() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String name = document.getString("name");
                    binding.tvWelcome.setText("Welcome, " + name);
                });
    }
}