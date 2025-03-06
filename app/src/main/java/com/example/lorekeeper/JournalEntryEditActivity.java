package com.example.lorekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class JournalEntryEditActivity extends AppCompatActivity {
    private EditText title_edit_text, text_edit_text;
    private String entryId;
    private ImageButton backbtn, save_button;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal_entry_edit2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        title_edit_text = findViewById(R.id.title_edit_text);
        text_edit_text = findViewById(R.id.text_edit_text);
        save_button = findViewById(R.id.save_button);
        backbtn = findViewById(R.id.backbtn);

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        entryId = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        text_edit_text.setText(text);
        title_edit_text.setText(title);

        backbtn.setOnClickListener(v -> onBackPressed());

        save_button.setOnClickListener(v -> saveEntryChanges());
    }
    private void saveEntryChanges() {
        String newText = text_edit_text.getText().toString().trim();
        String newTitle = title_edit_text.getText().toString().trim();

        if (newText.isEmpty() || newTitle.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("journal") // Измененный путь
                .document(entryId)
                .update("title", newTitle, "text", newText)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(JournalEntryEditActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JournalEntryEditActivity.this, "Ошибка при сохранении изменений", Toast.LENGTH_SHORT).show();
                });
    }
}