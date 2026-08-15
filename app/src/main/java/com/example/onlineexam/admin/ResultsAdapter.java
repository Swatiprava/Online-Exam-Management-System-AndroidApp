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

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private List<Map<String, Object>> resultList;

    public ResultsAdapter(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> result = resultList.get(position);

        // Firestore fields (Dhyan rakhein ki Firestore mein name yahi ho)
        String sName = (String) result.get("studentName");
        String sRoll = (String) result.get("rollNo");
        String title = (String) result.get("examTitle");

        Object score   = result.get("score");
        Object total   = result.get("totalMarks");
        Object correct = result.get("correctAnswers");
        Object totalQ  = result.get("totalQuestions");

        holder.tvStudentName.setText(sName != null ? sName : "Student Name");
        holder.tvRollNo.setText(sRoll != null ? "Roll No: " + sRoll : "Roll No: N/A");

        holder.tvExamTitle.setText(title != null ? title : "Exam Name");
        holder.tvScore.setText("Score: " + score + " / " + total);
        holder.tvCorrect.setText("Correct Answers: " + correct + " / " + totalQ);
    }

    @Override
    public int getItemCount() {
        return resultList != null ? resultList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExamTitle, tvScore, tvCorrect, tvStudentName, tvRollNo;

        ViewHolder(View v) {
            super(v);
            tvStudentName = v.findViewById(R.id.tvStudentName);
            tvRollNo      = v.findViewById(R.id.tvRollNo);
            tvExamTitle   = v.findViewById(R.id.tvExamTitle);
            tvScore       = v.findViewById(R.id.tvScore);
            tvCorrect     = v.findViewById(R.id.tvCorrect);
        }
    }
}