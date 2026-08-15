package com.example.onlineexam.teacher;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlineexam.BuildConfig;
import com.example.onlineexam.databinding.ActivityAddQuestionsBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddQuestionsActivity extends AppCompatActivity {

    private ActivityAddQuestionsBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> examList;
    private String selectedExamId = "";
    private String selectedSubjectName = "";

    private boolean isEditMode = false;
    private String questionId = "";

    private ActivityResultLauncher<Intent> csvPickerLauncher;

    // Gemini model - free tier. Change here if Google updates the recommended model name.
    private static final String GEMINI_MODEL = "gemini-2.0-flash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddQuestionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        examList = new ArrayList<>();

        if (getIntent().hasExtra("isEdit")) {
            isEditMode = getIntent().getBooleanExtra("isEdit", false);
            questionId = getIntent().getStringExtra("key");
            setupEditMode();
        }

        getSupportActionBar().setTitle(isEditMode ? "Update Question" : "Add Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        csvPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) importCsv(uri);
                    }
                });

        loadExams();

        binding.btnAddQuestion.setOnClickListener(v -> saveQuestion());

        binding.btnImportCsv.setOnClickListener(v -> {
            if (!hasValidExamSelected()) return;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            csvPickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
        });

        binding.btnGenerateAi.setOnClickListener(v -> {
            if (!hasValidExamSelected()) return;
            showGenerateAiDialog();
        });
    }

    private boolean hasValidExamSelected() {
        if (selectedExamId.isEmpty() || selectedSubjectName.isEmpty()) {
            Toast.makeText(this, "Please select an exam first (with a valid subject)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupEditMode() {
        binding.etQuestion.setText(getIntent().getStringExtra("question"));
        binding.etOptionA.setText(getIntent().getStringExtra("op1"));
        binding.etOptionB.setText(getIntent().getStringExtra("op2"));
        binding.etOptionC.setText(getIntent().getStringExtra("op3"));
        binding.etOptionD.setText(getIntent().getStringExtra("op4"));
        binding.etCorrectAnswer.setText(getIntent().getStringExtra("ans"));
        binding.btnAddQuestion.setText("Update Question");
        binding.btnImportCsv.setVisibility(View.GONE);
        binding.tvCsvHint.setVisibility(View.GONE);
        binding.btnGenerateAi.setVisibility(View.GONE);
    }

    private void saveQuestion() {
        String questionText = binding.etQuestion.getText().toString().trim();
        String optionA = binding.etOptionA.getText().toString().trim();
        String optionB = binding.etOptionB.getText().toString().trim();
        String optionC = binding.etOptionC.getText().toString().trim();
        String optionD = binding.etOptionD.getText().toString().trim();
        String correctAnswer = binding.etCorrectAnswer.getText().toString().trim().toUpperCase();

        if (selectedExamId.isEmpty()) {
            Toast.makeText(this, "Please select an exam", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSubjectName.isEmpty()) {
            Toast.makeText(this, "Selected exam has no subject set. Ask admin to fix the exam.", Toast.LENGTH_LONG).show();
            return;
        }
        if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty()
                || optionC.isEmpty() || optionD.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnAddQuestion.setEnabled(false);

        Map<String, Object> question = new HashMap<>();
        question.put("examId", selectedExamId);
        question.put("subject", selectedSubjectName);
        question.put("questionText", questionText);
        question.put("optionA", optionA);
        question.put("optionB", optionB);
        question.put("optionC", optionC);
        question.put("optionD", optionD);
        question.put("correctAnswer", correctAnswer);

        if (isEditMode) {
            db.collection("questions").document(questionId).update(question)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Question Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(this::handleError);
        } else {
            question.put("createdAt", System.currentTimeMillis());
            db.collection("questions").add(question)
                    .addOnSuccessListener(ref -> {
                        handleSuccess("Question Added!");
                        clearFields();
                    })
                    .addOnFailureListener(this::handleError);
        }
    }

    // ============ AI GENERATION LOGIC ============

    private void showGenerateAiDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etTopic = new EditText(this);
        etTopic.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etTopic.setHint("Topic e.g. Neural Networks basics");
        container.addView(etTopic);

        EditText etCount = new EditText(this);
        etCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCount.setHint("Number of questions (1-15)");
        etCount.setText("5");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = padding / 2;
        container.addView(etCount, lp);

        new AlertDialog.Builder(this)
                .setTitle("Generate Questions with AI")
                .setMessage("Subject: " + selectedSubjectName)
                .setView(container)
                .setPositiveButton("Generate", (dialog, which) -> {
                    String topic = etTopic.getText().toString().trim();
                    String countStr = etCount.getText().toString().trim();

                    if (topic.isEmpty()) {
                        Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int count;
                    try {
                        count = Integer.parseInt(countStr);
                    } catch (NumberFormatException e) {
                        count = 5;
                    }
                    count = Math.max(1, Math.min(count, 15)); // safety cap

                    generateWithAi(topic, count);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateWithAi(String topic, int count) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGenerateAi.setEnabled(false);
        Toast.makeText(this, "Generating questions...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String prompt = "Generate " + count + " multiple choice questions for an exam on the subject '"
                        + selectedSubjectName + "', specifically about the topic: '" + topic + "'. "
                        + "Return ONLY a valid JSON array, no markdown formatting, no code fences, no extra text. "
                        + "Each item must have exactly these keys: "
                        + "\"question\" (string), \"optionA\" (string), \"optionB\" (string), "
                        + "\"optionC\" (string), \"optionD\" (string), \"correctAnswer\" (one letter: A, B, C, or D).";

                JSONObject part = new JSONObject();
                part.put("text", prompt);
                JSONArray parts = new JSONArray();
                parts.put(part);
                JSONObject content = new JSONObject();
                content.put("parts", parts);
                JSONArray contents = new JSONArray();
                contents.put(content);
                JSONObject requestBody = new JSONObject();
                requestBody.put("contents", contents);

                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                        + GEMINI_MODEL + ":generateContent");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY);
                conn.setDoOutput(true);
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(30000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) responseBuilder.append(line);
                reader.close();

                if (responseCode < 200 || responseCode >= 300) {
                    String errMsg = responseBuilder.toString();
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnGenerateAi.setEnabled(true);
                        Toast.makeText(this, "AI error (" + responseCode + "): " + errMsg, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                JSONObject responseJson = new JSONObject(responseBuilder.toString());
                String rawText = responseJson
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                // Kabhi kabhi model markdown code fences (```json ... ```) bhi bhej deta hai - strip karo
                String cleaned = rawText.trim();
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.replaceAll("^```json", "").replaceAll("^```", "")
                            .replaceAll("```$", "").trim();
                }

                JSONArray questionsJson = new JSONArray(cleaned);
                List<Map<String, Object>> generatedQuestions = new ArrayList<>();

                for (int i = 0; i < questionsJson.length(); i++) {
                    JSONObject q = questionsJson.getJSONObject(i);
                    String qText = q.optString("question", "").trim();
                    String optA = q.optString("optionA", "").trim();
                    String optB = q.optString("optionB", "").trim();
                    String optC = q.optString("optionC", "").trim();
                    String optD = q.optString("optionD", "").trim();
                    String correctAns = q.optString("correctAnswer", "").trim().toUpperCase();

                    if (qText.isEmpty() || optA.isEmpty() || optB.isEmpty() || optC.isEmpty()
                            || optD.isEmpty() || !correctAns.matches("[ABCD]")) {
                        continue; // skip malformed entries
                    }

                    Map<String, Object> question = new HashMap<>();
                    question.put("examId", selectedExamId);
                    question.put("subject", selectedSubjectName);
                    question.put("questionText", qText);
                    question.put("optionA", optA);
                    question.put("optionB", optB);
                    question.put("optionC", optC);
                    question.put("optionD", optD);
                    question.put("correctAnswer", correctAns);
                    question.put("createdAt", System.currentTimeMillis());
                    question.put("source", "ai-generated");

                    generatedQuestions.add(question);
                }

                if (generatedQuestions.isEmpty()) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnGenerateAi.setEnabled(true);
                        Toast.makeText(this, "AI did not return valid questions. Try again.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    binding.btnGenerateAi.setEnabled(true);
                    uploadInBatches(generatedQuestions, 0);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnGenerateAi.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ============ END AI GENERATION LOGIC ============

    // ============ CSV IMPORT LOGIC ============

    private void importCsv(Uri uri) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnImportCsv.setEnabled(false);

        List<Map<String, Object>> validQuestions = new ArrayList<>();
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getContentResolver().openInputStream(uri)))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                List<String> fields = parseCsvLine(line);

                if (fields.size() != 6) {
                    skippedCount++;
                    continue;
                }

                String qText = fields.get(0).trim();
                String optA = fields.get(1).trim();
                String optB = fields.get(2).trim();
                String optC = fields.get(3).trim();
                String optD = fields.get(4).trim();
                String correctAns = fields.get(5).trim().toUpperCase();

                if (qText.isEmpty() || optA.isEmpty() || optB.isEmpty()
                        || optC.isEmpty() || optD.isEmpty()
                        || !correctAns.matches("[ABCD]")) {
                    skippedCount++;
                    continue;
                }

                Map<String, Object> question = new HashMap<>();
                question.put("examId", selectedExamId);
                question.put("subject", selectedSubjectName);
                question.put("questionText", qText);
                question.put("optionA", optA);
                question.put("optionB", optB);
                question.put("optionC", optC);
                question.put("optionD", optD);
                question.put("correctAnswer", correctAns);
                question.put("createdAt", System.currentTimeMillis());

                validQuestions.add(question);
            }

        } catch (IOException | SecurityException e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnImportCsv.setEnabled(true);
            Toast.makeText(this, "Could not read file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (validQuestions.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnImportCsv.setEnabled(true);
            Toast.makeText(this, "No valid questions found in this file. Check the format.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.btnImportCsv.setEnabled(true);
        uploadInBatches(validQuestions, skippedCount);
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    // ============ END CSV IMPORT LOGIC ============

    // ============ SHARED BATCH UPLOAD (used by both CSV + AI) ============

    private void uploadInBatches(List<Map<String, Object>> questions, int skippedCount) {
        binding.progressBar.setVisibility(View.VISIBLE);

        final int BATCH_SIZE = 450;
        int totalBatches = (int) Math.ceil(questions.size() / (double) BATCH_SIZE);
        final int[] completedBatches = {0};
        final boolean[] hadError = {false};

        for (int i = 0; i < questions.size(); i += BATCH_SIZE) {
            List<Map<String, Object>> chunk = questions.subList(i, Math.min(i + BATCH_SIZE, questions.size()));
            WriteBatch batch = db.batch();

            for (Map<String, Object> q : chunk) {
                DocumentReference newDocRef = db.collection("questions").document();
                batch.set(newDocRef, q);
            }

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        completedBatches[0]++;
                        if (completedBatches[0] == totalBatches) {
                            finishImport(questions.size(), skippedCount, hadError[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        hadError[0] = true;
                        completedBatches[0]++;
                        if (completedBatches[0] == totalBatches) {
                            finishImport(questions.size(), skippedCount, true);
                        }
                    });
        }
    }

    private void finishImport(int importedCount, int skippedCount, boolean hadError) {
        binding.progressBar.setVisibility(View.GONE);

        String msg;
        if (hadError) {
            msg = "Some questions failed to upload. Please check and try again.";
        } else {
            msg = importedCount + " questions added successfully!";
            if (skippedCount > 0) {
                msg += " (" + skippedCount + " rows skipped due to invalid format)";
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // ============ END SHARED BATCH UPLOAD ============

    private void handleSuccess(String msg) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnAddQuestion.setEnabled(true);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleError(Exception e) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnAddQuestion.setEnabled(true);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void loadExams() {
        db.collection("exams").whereEqualTo("isActive", true).get()
                .addOnSuccessListener(querySnapshot -> {
                    examList.clear();
                    List<String> examTitles = new ArrayList<>();
                    int selectedIndex = 0;
                    String oldExamId = getIntent().getStringExtra("examId");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> exam = new HashMap<>(doc.getData());
                        String docId = doc.getId();
                        exam.put("docId", docId);
                        examList.add(exam);
                        examTitles.add((String) doc.get("title"));

                        if (isEditMode && docId.equals(oldExamId)) {
                            selectedIndex = examList.size() - 1;
                        }
                    }

                    if (examTitles.isEmpty()) {
                        Toast.makeText(this,
                                "No active exams found. Ask admin to create an exam first.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_dropdown_item, examTitles);
                    binding.spinnerExam.setAdapter(adapter);
                    binding.spinnerExam.setSelection(selectedIndex);

                    updateLockedSubject(selectedIndex);

                    binding.spinnerExam.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            updateLockedSubject(position);
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                });
    }

    private void updateLockedSubject(int examPosition) {
        if (examPosition < 0 || examPosition >= examList.size()) return;

        selectedExamId = (String) examList.get(examPosition).get("docId");
        String examSubject = (String) examList.get(examPosition).get("subject");

        if (examSubject == null || examSubject.trim().isEmpty()) {
            selectedSubjectName = "";
            binding.tvLockedSubject.setText("Subject: (not set for this exam)");
        } else {
            selectedSubjectName = examSubject;
            binding.tvLockedSubject.setText("Subject: " + examSubject + "  🔒 (locked to exam)");
        }
    }

    private void clearFields() {
        binding.etQuestion.setText("");
        binding.etOptionA.setText("");
        binding.etOptionB.setText("");
        binding.etOptionC.setText("");
        binding.etOptionD.setText("");
        binding.etCorrectAnswer.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}