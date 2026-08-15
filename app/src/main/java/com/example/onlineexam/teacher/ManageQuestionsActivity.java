package com.example.onlineexam.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class ManageQuestionsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    QuestionAdapter adapter;
    ArrayList<QuestionModel> list;
    FirebaseFirestore db; // Change this
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_questions);

        recyclerView = findViewById(R.id.recyclerViewQuestions);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new QuestionAdapter(this, list);
        recyclerView.setAdapter(adapter);

        fetchQuestions();
    }

    private void fetchQuestions() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("questions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            list.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                QuestionModel model = document.toObject(QuestionModel.class);
                model.setKey(document.getId()); // Firestore Document ID set kar rahe hain
                list.add(model);
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}