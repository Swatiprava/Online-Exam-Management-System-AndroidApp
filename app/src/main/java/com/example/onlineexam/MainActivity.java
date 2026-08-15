package com.example.onlineexam;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Already logged in — role check karke route karo
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // Not logged in — login screen pe bhejo
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // MainActivity band karo
    }
}