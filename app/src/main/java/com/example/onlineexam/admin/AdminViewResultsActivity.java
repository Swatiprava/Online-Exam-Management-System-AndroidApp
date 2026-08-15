package com.example.onlineexam.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.onlineexam.databinding.ActivityAdminViewResultsBinding;
import com.example.onlineexam.student.MyResultsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewResultsActivity extends AppCompatActivity {

    private ActivityAdminViewResultsBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> resultList;
    private MyResultsAdapter adapter;

    private static final String TAG = "AdminViewResults";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminViewResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        resultList = new ArrayList<>();
        adapter = new MyResultsAdapter(resultList);
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(adapter);

        loadAllResults();
    }

    private void loadAllResults() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        // ✅ FIX: orderBy hata diya
        db.collection("results")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    resultList.clear();

                    Log.d(TAG, "Admin: Total results = " + querySnapshot.size());

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> result = new HashMap<>(doc.getData());
                        result.put("docId", doc.getId());
                        resultList.add(result);
                    }

                    adapter.notifyDataSetChanged();

                    if (resultList.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setText("No results available");
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}