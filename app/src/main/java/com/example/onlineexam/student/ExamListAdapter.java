package com.example.onlineexam.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import java.util.List;
import java.util.Map;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ExamViewHolder> {

    public interface OnExamClickListener {
        void onExamClick(Map<String, Object> exam);
    }

    private List<Map<String, Object>> examList;
    private OnExamClickListener listener;

    public ExamListAdapter(List<Map<String, Object>> examList, OnExamClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Map<String, Object> exam = examList.get(position);
        holder.tvTitle.setText((String) exam.get("title"));
        holder.tvSubject.setText("Subject: " + exam.get("subject"));
        holder.tvDuration.setText("Duration: " + exam.get("duration") + " mins");
        holder.tvMarks.setText("Total Marks: " + exam.get("totalMarks"));
        holder.btnStart.setOnClickListener(v -> listener.onExamClick(exam));
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubject, tvDuration, tvMarks;
        Button btnStart;

        ExamViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvMarks = itemView.findViewById(R.id.tvMarks);
            btnStart = itemView.findViewById(R.id.btnStart);
        }
    }
}