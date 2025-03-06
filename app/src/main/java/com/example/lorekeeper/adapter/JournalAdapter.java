package com.example.lorekeeper.adapter;

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

import com.example.lorekeeper.CampaignActivity;
import com.example.lorekeeper.JournalEntryEditActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.models.JournalEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder>{
    private List<JournalEntry> journalEntries;
    private FirebaseFirestore db;
    private Context context;


    public JournalAdapter(List<JournalEntry> journalEntries, Context context) {
        this.journalEntries = journalEntries;
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_entry, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        JournalEntry entry = journalEntries.get(position);
        holder.title.setText(entry.getTitle());
        holder.text.setText(entry.getText());
        if (entry.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(entry.getTimestamp().toDate());
            holder.timestamp.setText(formattedDate);
        } else {
            holder.timestamp.setText("Неизвестно");
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (entry.getCreatorId() != null && entry.getCreatorId().equals(currentUserId)) {
            holder.icDelete.setVisibility(View.VISIBLE);
            holder.icEdit.setVisibility(View.VISIBLE);

            holder.icDelete.setOnClickListener(v -> {
                db.collection("journal")
                        .document(entry.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            journalEntries.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        });
            });

            holder.icEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, JournalEntryEditActivity.class);
                intent.putExtra("id", entry.getId());
                intent.putExtra("text", entry.getText());
                intent.putExtra("title", entry.getTitle());
                context.startActivity(intent);
            });
        } else {
            holder.icDelete.setVisibility(View.GONE);
            holder.icEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public static class JournalViewHolder extends RecyclerView.ViewHolder {
        TextView title, text, timestamp;
        ImageButton icEdit, icDelete;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            text = itemView.findViewById(R.id.textViewPostText);
            timestamp = itemView.findViewById(R.id.textViewDate);
            icEdit = itemView.findViewById(R.id.ic_edit);
            icDelete = itemView.findViewById(R.id.ic_delete);
        }
    }
}
