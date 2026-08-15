package com.example.onlineexam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;


public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView tvInfo = findViewById(R.id.tvPasswordInfo);
        ProgressBar strength = findViewById(R.id.passwordStrength);
        TextView tvStrength = findViewById(R.id.tvStrength);

        binding.passwordLayout.setEndIconOnClickListener(v -> {

            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Strong Password Rules")
                    .setMessage(
                            "Password must contain:\n\n" +
                                    "✓ Minimum 8 characters\n" +
                                    "✓ One Uppercase letter (A-Z)\n" +
                                    "✓ One Lowercase letter (a-z)\n" +
                                    "✓ One Number (0-9)\n" +
                                    "✓ One Special Symbol (@#$%^&+=!)"
                    )
                    .setPositiveButton("OK", null)
                    .show();

        });


        binding.btnRegister.setOnClickListener(v -> {

            String name = binding.etName.getText().toString().trim();
            String rollNo = binding.etRollNo.getText().toString().trim();
            String branch = binding.etBranch.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Name Validation
            if (name.isEmpty()) {
                binding.etName.setError("Enter your name");
                binding.etName.requestFocus();
                return;
            }

            // Roll Number Validation
            if (rollNo.isEmpty()) {
                binding.etRollNo.setError("Enter Roll Number");
                binding.etRollNo.requestFocus();
                return;
            }

            // Branch Validation
            if (branch.isEmpty()) {
                binding.etBranch.setError("Enter Branch");
                binding.etBranch.requestFocus();
                return;
            }

            // Phone Validation
            if (phone.isEmpty()) {
                binding.etPhone.setError("Enter Phone Number");
                binding.etPhone.requestFocus();
                return;
            }

            // Valid Indian Mobile Number
            if (!phone.matches("^[6-9][0-9]{9}$")) {
                binding.etPhone.setError("Enter a valid 10-digit mobile number");
                binding.etPhone.requestFocus();
                return;
            }

            // Email Validation
            if (email.isEmpty()) {
                binding.etEmail.setError("Enter Email");
                binding.etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.setError("Enter a valid Email");
                binding.etEmail.requestFocus();
                return;
            }

            // Password Validation
            if (password.isEmpty()) {
                binding.etPassword.setError("Enter Password");
                binding.etPassword.requestFocus();
                return;
            }

            String PASSWORD_PATTERN =
                    "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

            if (!password.matches(PASSWORD_PATTERN)) {

                binding.etPassword.setError(
                        "Password must contain:\n" +
                                "• 8+ characters\n" +
                                "• 1 Capital Letter\n" +
                                "• 1 Small Letter\n" +
                                "• 1 Number\n" +
                                "• 1 Special Symbol");

                binding.etPassword.requestFocus();

                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);

            // Duplicate Roll Number Check
            db.collection("users")
                    .whereEqualTo("rollNo", rollNo)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (!snapshot.isEmpty()) {

                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnRegister.setEnabled(true);

                            binding.etRollNo.setError("Roll Number already exists");
                            binding.etRollNo.requestFocus();
                            return;
                        }

                        // Create Firebase Auth User
                        auth.createUserWithEmailAndPassword(email, password)

                                .addOnSuccessListener(authResult -> {

                                    String uid = auth.getCurrentUser().getUid();

                                    Map<String, Object> user = new HashMap<>();

                                    user.put("uid", uid);
                                    user.put("name", name);
                                    user.put("rollNo", rollNo);
                                    user.put("branch", branch);
                                    user.put("phone", phone);
                                    user.put("email", email);
                                    user.put("role", "student");

                                    user.put("examsDone", 0);
                                    user.put("avgScore", 0);
                                    user.put("upcoming", 0);

                                    db.collection("users")
                                            .document(uid)
                                            .set(user)

                                            .addOnSuccessListener(unused -> {

                                                binding.progressBar.setVisibility(View.GONE);

                                                Toast.makeText(RegisterActivity.this,
                                                        "Registration Successful",
                                                        Toast.LENGTH_SHORT).show();

                                                startActivity(new Intent(RegisterActivity.this,
                                                        com.example.onlineexam.student.StudentDashboardActivity.class));

                                                finish();

                                            })

                                            .addOnFailureListener(e -> {

                                                binding.progressBar.setVisibility(View.GONE);
                                                binding.btnRegister.setEnabled(true);

                                                Toast.makeText(RegisterActivity.this,
                                                        e.getMessage(),
                                                        Toast.LENGTH_LONG).show();

                                            });

                                })

                                .addOnFailureListener(e -> {

                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.btnRegister.setEnabled(true);

                                    Toast.makeText(RegisterActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_LONG).show();

                                });

                    })

                    .addOnFailureListener(e -> {

                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);

                        Toast.makeText(RegisterActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show();

                    });

        });
        binding.tvLogin.setOnClickListener(v -> finish());
    }
}