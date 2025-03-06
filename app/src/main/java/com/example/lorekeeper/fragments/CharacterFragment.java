package com.example.lorekeeper.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lorekeeper.CampaignAddActivity;
import com.example.lorekeeper.CharacterAddActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.adapter.CharacterAdapter;
import com.example.lorekeeper.adapter.campaignAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.lorekeeper.models.Character;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CharacterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterFragment extends Fragment {
    private RecyclerView recyclerView;
    private CharacterAdapter characterAdapter;
    private List<Character> characterList;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchInput;
    private FirebaseAuth auth;
    private TextView toolbarTitle;
    public CharacterFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchInput = view.findViewById(R.id.search_input);
//        toolbarTitle = view.findViewById(R.id.character_title);
        characterList = new ArrayList<>();
        characterAdapter = new CharacterAdapter(characterList, getContext(), false, null);
        recyclerView.setAdapter(characterAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        view.findViewById(R.id.btn_add).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CharacterAddActivity.class);
            startActivity(intent);
        });

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadCharacters();
        setupSearch();

        return view;
    }
    private void loadCharacters() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("characters")
                .whereEqualTo("creatorId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Character> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Character character = document.toObject(Character.class);
                            if (character != null) {
                                character.setId(document.getId());
                                newList.add(character);
                            }
                        }
                        characterAdapter.updateData(newList);
                    }
                });
    }
    private void refreshData() {
        loadCharacters();

        swipeRefreshLayout.setRefreshing(false);
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

}