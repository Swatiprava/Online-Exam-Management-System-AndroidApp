package com.example.onlineexam.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import java.util.List;
import java.util.Map;

public class MyResultsAdapter extends RecyclerView.Adapter<MyResultsAdapter.VH> {

    private List<Map<String, Object>> list;

    public MyResultsAdapter(List<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> data = list.get(pos);

        // --- NEW: Student Details fetch kar rahe hain ---
        String sName = data.get("studentName") != null ? String.valueOf(data.get("studentName")) : "Student Name";
        String sRoll = data.get("rollNo") != null ? String.valueOf(data.get("rollNo")) : "N/A";

        if (h.tvStudentName != null) h.tvStudentName.setText(sName);
        if (h.tvRollNo != null) h.tvRollNo.setText("Roll No: " + sRoll);
        // ------------------------------------------------

        // Exam Title
        String title = data.get("examTitle") != null ? String.valueOf(data.get("examTitle")) : "Exam";
        h.title.setText(title);

        // Score Calculation
        Object scoreObj = data.get("score");
        Object totalObj = data.get("totalMarks");

        long score = 0;
        long total = 0;

        if (scoreObj instanceof Number) score = ((Number) scoreObj).longValue();
        if (totalObj instanceof Number) total = ((Number) totalObj).longValue();

        h.score.setText("Score: " + score + " / " + total);

        // Percentage and Pass/Fail logic
        int pct = total > 0 ? (int) ((score * 100) / total) : 0;
        if (h.percent != null) {
            // Yahan hum percentage aur correct answers dono dikha sakte hain
            Object correct = data.get("correctAnswers");
            Object totalQ = data.get("totalQuestions");

            if (correct != null && totalQ != null) {
                h.percent.setText("Correct: " + correct + " / " + totalQ + " (" + pct + "%)");
            } else {
                h.percent.setText(pct + "% — " + (pct >= 50 ? "Pass" : "Fail"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        // tvStudentName aur tvRollNo ko add kiya gaya hai
        TextView title, score, percent, tvStudentName, tvRollNo;

        VH(View v) {
            super(v);
            // In IDs ko item_result.xml se match hona chahiye
            tvStudentName = v.findViewById(R.id.tvStudentName);
            tvRollNo      = v.findViewById(R.id.tvRollNo);

            title   = v.findViewById(R.id.tvExamTitle);
            score   = v.findViewById(R.id.tvScore);
            percent = v.findViewById(R.id.tvCorrect); // XML mein iska ID tvCorrect hai
        }
    }
}