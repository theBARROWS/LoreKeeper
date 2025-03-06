package com.example.lorekeeper.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lorekeeper.CampaignAddActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.adapter.campaignAdapter;
import com.example.lorekeeper.models.Campaign;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class CampaignFragment extends Fragment {
    private EditText searchInput;
    private RecyclerView recyclerView;
    private campaignAdapter adapter;
    private List<Campaign> campaignList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaign, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        view.findViewById(R.id.btn_add).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CampaignAddActivity.class);
            startActivity(intent);
        });

        adapter = new campaignAdapter(campaignList, getContext());
        recyclerView.setAdapter(adapter);

        loadCampaigns();
        setupSearch();
    }
    private void init(View view){
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        searchInput = view.findViewById(R.id.search_input);

        if(getActivity() != null) ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        ??????????? я не знаю надо ли это? тулбар выше


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void refreshData() {
        loadCampaigns();

        swipeRefreshLayout.setRefreshing(false);
    }
    private void loadCampaigns() {
        firestore.collection("campaigns")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Campaign> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Campaign campaign = document.toObject(Campaign.class);
                            if (campaign != null) {
                                campaign.setId(document.getId());

                                if (campaign.getCreatorId().equals(user.getUid())) {
                                    campaign.setEditable(true);
                                } else {
                                    campaign.setEditable(false);
                                }

                                newList.add(campaign);
                            }
                        }
                        adapter.updateData(newList);
                    }
                });
    }
}