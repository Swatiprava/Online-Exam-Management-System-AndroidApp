package com.example.onlineexam.teacher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.MyViewHolder> {

    Context context;
    ArrayList<QuestionModel> list;

    public QuestionAdapter(Context context, ArrayList<QuestionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        QuestionModel model = list.get(position);

        holder.question.setText(model.getQuestionText()); // Aapke Firestore field ka naam 'questionText' hai
        holder.ans.setText("Ans: " + model.getCorrectAnswer());

        // DELETE Logic (Firestore)
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("questions")
                    .document(model.getKey()).delete()
                    .addOnSuccessListener(aVoid -> {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Deleted from Firestore", Toast.LENGTH_SHORT).show();
                    });
        });

        // EDIT Logic
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddQuestionsActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("key", model.getKey());
            intent.putExtra("question", model.getQuestionText());
            intent.putExtra("op1", model.getOptionA());
            intent.putExtra("op2", model.getOptionB());
            intent.putExtra("op3", model.getOptionC());
            intent.putExtra("op4", model.getOptionD());
            intent.putExtra("ans", model.getCorrectAnswer());
            intent.putExtra("examId", model.getExamId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView question, ans;
        ImageButton btnEdit, btnDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.tvQuestionText);
            ans = itemView.findViewById(R.id.tvAnswerText);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}