package com.example.lorekeeper;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lorekeeper.adapter.CharacterAdapter;
import com.example.lorekeeper.models.Character;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CampaignCharacterSearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText searchInput;
    private CharacterAdapter characterAdapter;
    private List<com.example.lorekeeper.models.Character> characterList;
    private FirebaseFirestore db;
    private ImageButton backButton;
    private String campaignId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_campaign_character_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchInput = findViewById(R.id.search_input);

        backButton = findViewById(R.id.backbtn);
        backButton.setOnClickListener(v -> onBackPressed());

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        db = FirebaseFirestore.getInstance();
        campaignId = getIntent().getStringExtra("campaignId");

        auth = FirebaseAuth.getInstance();
        characterList = new ArrayList<>();
        characterAdapter = new CharacterAdapter(characterList, this, true, campaignId);
//        characterAdapter = new CharacterAdapter(characterList, this, false, null);
        recyclerView.setAdapter(characterAdapter);

        loadCharacters();
        setupSearch();
    }
    private void loadCharacters() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("campaigns").document(campaignId)
                .get()
                .addOnCompleteListener(campaignTask -> {
                    if (campaignTask.isSuccessful()) {
                        DocumentSnapshot campaignDocument = campaignTask.getResult();
                        final List<String> existingCharacterIds = (List<String>) campaignDocument.get("characterIds");

//                        if (existingCharacterIds == null) {
//                            existingCharacterIds = new ArrayList<>();
//                        }

                        db.collection("characters")
                                .whereNotEqualTo("creatorId", user.getUid())
                                .get()
                                .addOnCompleteListener(characterTask -> {
                                    if (characterTask.isSuccessful()) {
                                        List<Character> newList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : characterTask.getResult()) {
                                            Character character = document.toObject(Character.class);
                                            character.setId(document.getId());

                                            if (!existingCharacterIds.contains(character.getId())) {
                                                newList.add(character);
                                            }
                                        }
                                        characterAdapter.updateData(newList);
                                    }
                                });
                    }
                });
    }
    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                characterAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void refreshData() {
        loadCharacters();

        swipeRefreshLayout.setRefreshing(false);
    }

}