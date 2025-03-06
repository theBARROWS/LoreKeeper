package com.example.lorekeeper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lorekeeper.models.JournalEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalEntryAddActivity extends AppCompatActivity {
    private EditText titleEditText, textEditText;
    private ImageButton saveButton, backButton;
    private FirebaseFirestore db;
    private String campaignId;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal_entry_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        titleEditText = findViewById(R.id.title_edit_text);
        textEditText = findViewById(R.id.text_edit_text);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.backbtn);

        db = FirebaseFirestore.getInstance();

        campaignId = getIntent().getStringExtra("campaignId");

        saveButton.setOnClickListener(v -> saveJournalEntry());
        backButton.setOnClickListener(v -> onBackPressed());
    }
    private void saveJournalEntry() {
        String title = titleEditText.getText().toString().trim();
        String text = textEditText.getText().toString().trim();

        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

//        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//        Timestamp timestamp = new Timestamp(new Date());
        String entryId = db.collection("journal").document().getId();
        String userId = user.getUid();
        Timestamp timestamp = Timestamp.now();
        JournalEntry journalEntry = new JournalEntry(entryId, campaignId, title, text, timestamp, userId);

        db.collection("journal")
                .document(entryId)
                .set(journalEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при добавлении записи", Toast.LENGTH_SHORT).show();
                });
    }
}