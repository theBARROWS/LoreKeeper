package com.example.lorekeeper.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.lorekeeper.CampaignCharacterSearchActivity;
import com.example.lorekeeper.CharacterAddActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.adapter.CharacterAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.lorekeeper.models.Character;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CampaignCharactersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CampaignCharactersFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CharacterAdapter characterAdapter;
    private FirebaseFirestore firestore;
    private List<Character> characterList;
    private FirebaseAuth auth;

    private TextView noCharactersText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String ARG_CAMPAIGN_ID = "campaignId";
    private String campaignId;
    private FirebaseUser user;
    private String creatorId;
    private ImageButton addButton;
    public static CampaignCharactersFragment newInstance(String campaignId) {
        CampaignCharactersFragment fragment = new CampaignCharactersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMPAIGN_ID, campaignId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaign_characters, container, false);
        if (getArguments() != null) {
            campaignId = getArguments().getString("campaignId");
        }
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.characters_recycler_view);
        noCharactersText = view.findViewById(R.id.no_characters_text);

        characterList = new ArrayList<>();
        characterAdapter = new CharacterAdapter(characterList, getContext(), false, campaignId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(characterAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        swipeRefreshLayout.setOnRefreshListener(this::loadCampaignCharacters);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        addButton = view.findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CampaignCharacterSearchActivity.class);
            intent.putExtra("campaignId", campaignId);
            startActivity(intent);
        });
        loadCampaignDetails();
        loadCampaignCharacters();

        return view;
    }
    private void loadCampaignDetails() {
        if (campaignId == null || user == null) return;

        db.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        creatorId = documentSnapshot.getString("creatorId");

                        if (creatorId != null && creatorId.equals(user.getUid())) {
                            characterAdapter.setCreatorId(creatorId);
                            addButton.setVisibility(View.VISIBLE);
                        } else {
                            addButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void loadCampaignCharacters() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || campaignId == null) return;

        firestore.collection("campaigns")
                .document(campaignId)
                .get()
                .addOnSuccessListener(campaignSnapshot -> {
                    if (campaignSnapshot.exists()) {
                        List<String> characterIds = (List<String>) campaignSnapshot.get("characterIds");
                        if (characterIds != null && !characterIds.isEmpty()) {
                            loadCharactersByIds(characterIds);
                        } else {
                            showNoCharactersMessage();
                        }
                    } else {
                        showNoCharactersMessage();
                    }
                })
                .addOnCompleteListener(task -> swipeRefreshLayout.setRefreshing(false))
                .addOnFailureListener(e -> {
                    Log.e("CampaignCharacters", "Error getting campaign", e);
                    showNoCharactersMessage();
                });
    }
    //я ненавижу эту функцию загрузки по айди всей своей душой
    private void loadCharactersByIds(List<String> characterIds) {
        firestore.collection("characters")
                .whereIn(FieldPath.documentId(), characterIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoCharactersMessage();
                    } else {
                        List<Character> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Character character = document.toObject(Character.class);
                            if (character != null) {
                                character.setId(document.getId());
                                newList.add(character);
                            }
                        }
                        characterAdapter.updateData(newList);
                        showCharactersList();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CampaignCharacters", "Error getting characters", e);
                    showNoCharactersMessage();
                });
    }

    private void refreshData() {
        loadCampaignCharacters();
    }

    private void showNoCharactersMessage() {
        recyclerView.setVisibility(View.GONE);
        noCharactersText.setVisibility(View.VISIBLE);
    }
    private void showCharactersList() {
        recyclerView.setVisibility(View.VISIBLE);
        noCharactersText.setVisibility(View.GONE);
    }
}