package com.example.onlineexam.student;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeExamActivity extends AppCompatActivity {

    private TextView tvTimer, tvQuestionNo, tvQuestionText;
    private RadioGroup radioGroup;
    private RadioButton rbA, rbB, rbC, rbD;
    private Button btnNext;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<Map<String, Object>> questionList = new ArrayList<>();
    private Map<Integer, String> selectedAnswers = new HashMap<>();

    private int currentIndex = 0;
    private String examId, examTitle;
    private int duration, totalMarks;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_exam);

        // Intent se data lo
        examId     = getIntent().getStringExtra("examId");
        examTitle  = getIntent().getStringExtra("examTitle");
        duration   = getIntent().getIntExtra("duration", 30);
        totalMarks = getIntent().getIntExtra("totalMarks", 100);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(examTitle);
        }

        // Views
        tvTimer        = findViewById(R.id.tvTimer);
        tvQuestionNo   = findViewById(R.id.tvQuestionNo);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        radioGroup     = findViewById(R.id.radioGroup);
        rbA            = findViewById(R.id.rbA);
        rbB            = findViewById(R.id.rbB);
        rbC            = findViewById(R.id.rbC);
        rbD            = findViewById(R.id.rbD);
        btnNext        = findViewById(R.id.btnNext);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadQuestions();

        btnNext.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentIndex < questionList.size() - 1) {
                currentIndex++;
                showQuestion(currentIndex);
            } else {
                submitExam();
            }
        });
    }


    private void loadQuestions() {
        db.collection("questions")
                .whereEqualTo("examId", examId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    questionList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> q = new HashMap<>(doc.getData());
                        q.put("docId", doc.getId());
                        questionList.add(q);
                    }
                    if (questionList.isEmpty()) {
                        Toast.makeText(this,
                                "No questions found for this exam!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showQuestion(0);
                        startTimer(duration);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error loading questions: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }


    private void showQuestion(int index) {
        Map<String, Object> q = questionList.get(index);

        tvQuestionNo.setText("Question " + (index + 1) + " of " + questionList.size());
        tvQuestionText.setText((String) q.get("questionText"));

        rbA.setText("A.  " + getOption(q, "optionA", "option_a"));
        rbB.setText("B.  " + getOption(q, "optionB", "option_b"));
        rbC.setText("C.  " + getOption(q, "optionC", "option_c"));
        rbD.setText("D.  " + getOption(q, "optionD", "option_d"));

        // Pehle se selected answer restore karo
        radioGroup.clearCheck();
        String prev = selectedAnswers.get(index);
        if (prev != null) {
            switch (prev) {
                case "A": rbA.setChecked(true); break;
                case "B": rbB.setChecked(true); break;
                case "C": rbC.setChecked(true); break;
                case "D": rbD.setChecked(true); break;
            }
        }

        // Last question pe Submit button dikhao
        btnNext.setText(index == questionList.size() - 1 ? "Submit" : "Next");
    }


    private String getOption(Map<String, Object> q, String key1, String key2) {
        Object val = q.get(key1);
        if (val == null) val = q.get(key2);
        return val != null ? val.toString() : "N/A";
    }


    private void saveCurrentAnswer() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if      (selectedId == R.id.rbA) selectedAnswers.put(currentIndex, "A");
        else if (selectedId == R.id.rbB) selectedAnswers.put(currentIndex, "B");
        else if (selectedId == R.id.rbC) selectedAnswers.put(currentIndex, "C");
        else if (selectedId == R.id.rbD) selectedAnswers.put(currentIndex, "D");
    }


    private void startTimer(int minutes) {
        long millis = (long) minutes * 60 * 1000;
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / 60000;
                long secs = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format("Time: %02d:%02d", mins, secs));
            }
            @Override
            public void onFinish() {
                Toast.makeText(TakeExamActivity.this,
                        "Time up! Auto submitting...",
                        Toast.LENGTH_SHORT).show();
                submitExam();
            }
        }.start();
    }


    private void submitExam() {
        if (countDownTimer != null) countDownTimer.cancel();

        saveCurrentAnswer();

        int correct = 0;
        for (int i = 0; i < questionList.size(); i++) {
            String selected = selectedAnswers.get(i);
            String answer   = (String) questionList.get(i).get("correctAnswer");
            if (selected != null && selected.equalsIgnoreCase(answer)) correct++;
        }

        int marksPerQ     = totalMarks / questionList.size();
        int scoreObtained = correct * marksPerQ;

        // NOTE: studentName / rollNo yahan JAANBUJH KAR nahi daale gaye.
        // Ye "denormalized snapshot" ban jaata tha aur profile edit hone ke baad
        // purane results mein purana naam dikhta rehta tha (yehi bug tha).
        // studentId hi single source of truth hai - naam hamesha
        // users collection se live fetch hoga jab display karna ho.
        Map<String, Object> result = new HashMap<>();
        result.put("examId",         examId);
        result.put("examTitle",      examTitle);
        result.put("studentId",      auth.getCurrentUser().getUid());
        result.put("correctAnswers", correct);
        result.put("totalQuestions", questionList.size());
        result.put("score",          scoreObtained);
        result.put("totalMarks",     totalMarks);
        result.put("submittedAt",    com.google.firebase.Timestamp.now());

        db.collection("results")
                .add(result)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this,
                            "Submitted Successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error submitting: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}