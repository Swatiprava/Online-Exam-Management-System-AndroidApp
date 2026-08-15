package com.example.onlineexam.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.onlineexam.databinding.ActivityManageSubjectsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageSubjectsActivity extends AppCompatActivity {

    private ActivityManageSubjectsBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> subjectList;
    private SubjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageSubjectsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Subjects");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        subjectList = new ArrayList<>();

        adapter = new SubjectAdapter(subjectList, this::confirmDelete);
        binding.recyclerSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSubjects.setAdapter(adapter);

        binding.fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        loadSubjects();
    }

    private void loadSubjects() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("subjects")
                .orderBy("name")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    subjectList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> subject = new HashMap<>(doc.getData());
                        subject.put("docId", doc.getId());
                        subjectList.add(subject);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(subjectList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddSubjectDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("e.g. DBMS, OOP, Android Dev");

        new AlertDialog.Builder(this)
                .setTitle("Add New Subject")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String subjectName = input.getText().toString().trim();
                    if (subjectName.isEmpty()) {
                        Toast.makeText(this, "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addSubject(subjectName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addSubject(String subjectName) {
        // Duplicate check taaki same subject do baar na add ho jaye
        db.collection("subjects")
                .whereEqualTo("name", subjectName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(this, "This subject already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> subject = new HashMap<>();
                    subject.put("name", subjectName);
                    subject.put("createdAt", System.currentTimeMillis());

                    db.collection("subjects").add(subject)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Subject Added!", Toast.LENGTH_SHORT).show();
                                loadSubjects();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void confirmDelete(String docId, String subjectName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Subject")
                .setMessage("Delete \"" + subjectName + "\"? Questions already using this subject will keep the old name, but it won't be selectable for new questions.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("subjects").document(docId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Subject Deleted", Toast.LENGTH_SHORT).show();
                                loadSubjects();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}