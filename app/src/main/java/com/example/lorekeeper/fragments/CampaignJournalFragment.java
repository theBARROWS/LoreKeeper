package com.example.lorekeeper.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lorekeeper.JournalEntryAddActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.adapter.JournalAdapter;
import com.example.lorekeeper.models.JournalEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CampaignJournalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CampaignJournalFragment extends Fragment {

    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> journalEntries;
    private TextView noCharactersText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore db;
    private AppCompatImageButton addButton;

    private static final String ARG_CAMPAIGN_ID = "campaignId";
    private String campaignId;
    private FirebaseUser user;
    private String creatorId;

    public CampaignJournalFragment() {
    }

    public static CampaignJournalFragment newInstance(String campaignId) {
        CampaignJournalFragment fragment = new CampaignJournalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMPAIGN_ID, campaignId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            campaignId = getArguments().getString(ARG_CAMPAIGN_ID);
        }
        db = FirebaseFirestore.getInstance();
        journalEntries = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaign_journal, container, false);

        noCharactersText = view.findViewById(R.id.no_characters_text);
        recyclerView = view.findViewById(R.id.journal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new JournalAdapter(journalEntries, getContext());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        addButton = view.findViewById(R.id.add_journal_entry_button);
        loadJournalEntries();
        loadCampaignDetails();

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), JournalEntryAddActivity.class);
            intent.putExtra("campaignId", campaignId);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(this::loadJournalEntries);

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
                            addButton.setVisibility(View.VISIBLE);
                        } else {
                            addButton.setVisibility(View.GONE);
                        }
                    }
                });
    }
    private void loadJournalEntries() {
        journalEntries.clear();
        db.collection("journal")
                .whereEqualTo("campaignId", campaignId)
//                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<JournalEntry> newEntries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            JournalEntry entry = document.toObject(JournalEntry.class);
                            newEntries.add(entry);
                        }

                        journalEntries.clear();
                        journalEntries.addAll(newEntries);

                        if (journalEntries.isEmpty()) {
                            showNoCharactersMessage();
                        } else {
                            showCharactersList();
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("CampaignJournalFragment", "Error loading journal entries", task.getException());
                    }
                })
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                });
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