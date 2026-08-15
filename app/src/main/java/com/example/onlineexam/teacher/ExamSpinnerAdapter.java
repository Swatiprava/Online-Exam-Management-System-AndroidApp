package com.example.onlineexam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ExamSpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> examNames;
    private final List<String> examIds;

    public ExamSpinnerAdapter(Context context, List<String> examNames, List<String> examIds) {
        super(context, android.R.layout.simple_spinner_item, examNames);
        this.context = context;
        this.examNames = examNames;
        this.examIds = examIds;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(examNames.get(position));
        return textView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setText(examNames.get(position));
        return textView;
    }

    public String getExamId(int position) {
        return examIds.get(position);
    }

    public String getExamName(int position) {
        return examNames.get(position);
    }
}