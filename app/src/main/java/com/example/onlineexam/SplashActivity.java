package com.example.onlineexam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineexam.LoginActivity;
import com.example.onlineexam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

// Import your dashboard activities from their sub-packages
import com.example.onlineexam.admin.AdminDashboardActivity;
import com.example.onlineexam.student.StudentDashboardActivity;
import com.example.onlineexam.teacher.TeacherDashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser == null) {
                // Not logged in, go to Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                // If you have logic to check user type (Admin/Student/Teacher),
                // you would put it here. Otherwise, go to a default dashboard:
                startActivity(new Intent(SplashActivity.this, StudentDashboardActivity.class));
            }
            finish();
        }, 3000);
    }
}