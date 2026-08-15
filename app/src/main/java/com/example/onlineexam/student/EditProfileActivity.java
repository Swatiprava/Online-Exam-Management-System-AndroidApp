package com.example.onlineexam.student;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineexam.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadProfile();

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()

                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        binding.etName.setText(document.getString("name"));
                        binding.etPhone.setText(document.getString("phone"));
                        binding.etBranch.setText(document.getString("branch"));

                    }

                });

    }

    private void saveProfile() {

        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String branch = binding.etBranch.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Enter your name");
            binding.etName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            binding.etPhone.setError("Enter phone number");
            binding.etPhone.requestFocus();
            return;
        }

        // Indian mobile number validation
        if (!phone.matches("^[6-9][0-9]{9}$")) {
            binding.etPhone.setError("Enter a valid 10-digit mobile number");
            binding.etPhone.requestFocus();
            return;
        }

        if (branch.isEmpty()) {
            binding.etBranch.setError("Enter branch");
            binding.etBranch.requestFocus();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        map.put("branch", branch);

        db.collection("users")
                .document(uid)
                .update(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}