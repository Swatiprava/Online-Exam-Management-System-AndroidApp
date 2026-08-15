package com.example.onlineexam.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import java.util.List;
import java.util.Map;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(String docId, String subjectName);
    }

    private final List<Map<String, Object>> subjectList;
    private final OnDeleteClickListener deleteListener;

    public SubjectAdapter(List<Map<String, Object>> subjectList, OnDeleteClickListener deleteListener) {
        this.subjectList = subjectList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> subject = subjectList.get(position);
        String name = (String) subject.get("name");
        String docId = (String) subject.get("docId");

        holder.tvSubjectName.setText(name);
        holder.ivDelete.setOnClickListener(v -> deleteListener.onDeleteClick(docId, name));
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName;
        ImageView ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}