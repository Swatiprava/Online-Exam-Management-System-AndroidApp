package com.example.onlineexam;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences; // Import added
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.databinding.ActivityLoginBinding;
import com.example.onlineexam.admin.AdminDashboardActivity;
import com.example.onlineexam.teacher.TeacherDashboardActivity;
import com.example.onlineexam.student.StudentDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(document -> {
                                    binding.progressBar.setVisibility(View.GONE);

                                    if (document.exists()) {
                                        // 1. Firestore se data nikalein
                                        String role = document.getString("role");
                                        String name = document.getString("name");
                                        String roll = document.getString("rollNo");

                                        if (role == null) role = "student";

                                        // 2. Data ko SharedPreferences mein save karein (Important)
                                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("userName", name != null ? name : "Unknown");
                                        editor.putString("userRoll", roll != null ? roll : "N/A");
                                        editor.apply();

                                        // 3. Dashboards par bhejein
                                        Intent intent;
                                        switch (role) {
                                            case "admin":
                                                intent = new Intent(this, AdminDashboardActivity.class);
                                                break;
                                            case "teacher":
                                                intent = new Intent(this, TeacherDashboardActivity.class);
                                                break;
                                            default:
                                                intent = new Intent(this, StudentDashboardActivity.class);
                                                break;
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // NAYA: Forgot Password click listener
        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Enter your registered email");

        // Agar login form mein pehle se email likha hai, wahi pre-fill kar do
        String existingEmail = binding.etEmail.getText().toString().trim();
        if (!existingEmail.isEmpty()) {
            input.setText(existingEmail);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your email address. We'll send you a link to reset your password.")
                .setView(input)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "If an account exists for " + email + ", a reset link has been sent.",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}