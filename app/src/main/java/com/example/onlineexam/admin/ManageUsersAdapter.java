package com.example.onlineexam.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlineexam.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ManageUsersAdapter extends RecyclerView.Adapter<ManageUsersAdapter.UserViewHolder> {

    private List<Map<String, Object>> userList;
    private FirebaseFirestore db;

    public ManageUsersAdapter(List<Map<String, Object>> userList, FirebaseFirestore db) {
        this.userList = userList;
        this.db = db;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = userList.get(position);
        holder.tvName.setText((String) user.get("name"));
        holder.tvEmail.setText((String) user.get("email"));

        String currentRole = (String) user.get("role");
        List<String> roles = Arrays.asList("student", "teacher", "admin");
        int roleIndex = roles.indexOf(currentRole);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );
        holder.spinnerRole.setAdapter(spinnerAdapter);
        if (roleIndex >= 0) holder.spinnerRole.setSelection(roleIndex);

        holder.btnUpdate.setOnClickListener(v -> {
            String newRole = holder.spinnerRole.getSelectedItem().toString();
            String docId = (String) user.get("docId");
            db.collection("users").document(docId)
                    .update("role", newRole)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(holder.itemView.getContext(), "Role updated!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Spinner spinnerRole;
        Button btnUpdate;

        UserViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            spinnerRole = itemView.findViewById(R.id.spinnerRole);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }
    }
}