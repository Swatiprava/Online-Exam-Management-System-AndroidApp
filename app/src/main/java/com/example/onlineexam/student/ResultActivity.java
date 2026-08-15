package com.example.onlineexam.student;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Exam Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        int obtainedMarks = getIntent().getIntExtra("obtainedMarks", 0);
        int totalMarks = getIntent().getIntExtra("totalMarks", 0);
        String examTitle = getIntent().getStringExtra("examTitle");

        double percentage = totalMarks > 0 ? (obtainedMarks * 100.0) / totalMarks : 0;
        boolean isPassed = percentage >= 50;

        binding.tvExamTitle.setText(examTitle);
        binding.tvObtainedMarks.setText(obtainedMarks + " / " + totalMarks);
        binding.tvPercentage.setText(String.format("%.1f%%", percentage));

        if (isPassed) {
            binding.tvStatus.setText("CONGRATULATIONS!\nYou Passed!");
            binding.tvStatus.setTextColor(0xFF2E7D32);
            binding.resultCard.setCardBackgroundColor(0xFFE8F5E9);
        } else {
            binding.tvStatus.setText("Better Luck Next Time!\nYou Failed");
            binding.tvStatus.setTextColor(0xFFE53935);
            binding.resultCard.setCardBackgroundColor(0xFFFFEBEE);
        }

        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnViewResults.setOnClickListener(v -> {
            startActivity(new Intent(this, MyResultsActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}