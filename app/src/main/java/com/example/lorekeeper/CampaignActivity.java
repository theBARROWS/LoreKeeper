package com.example.lorekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;

import com.example.lorekeeper.adapter.CampaignPagerAdapter;
import com.example.lorekeeper.models.Campaign;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CampaignActivity extends AppCompatActivity {
    private TextView title, description;
    private FirebaseFirestore firestore;
    private String campaignId;
    private ImageView header_image2;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseAuth auth;
    private String creatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_campaign);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        header_image2= findViewById(R.id.header_image2);
        title = findViewById(R.id.campaign_title);
        description = findViewById(R.id.campaign_description);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        campaignId = getIntent().getStringExtra("campaignId");
        auth = FirebaseAuth.getInstance();
        CampaignPagerAdapter adapter = new CampaignPagerAdapter(this, campaignId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("журнал");
                            break;
                        case 1:
                            tab.setText("персонажи");
                            break;
                    }
                }).attach();

        firestore = FirebaseFirestore.getInstance();



        if (campaignId != null) {
            loadCampaignDetails();
        }

        ImageButton deleteBtn = findViewById(R.id.deletebtn);
        deleteBtn.setOnClickListener(v -> onDeleteCampaign());

        ImageButton editBtn = findViewById(R.id.editbtn);
        editBtn.setOnClickListener(v -> onEditCampaign());
    }
    public void onEditCampaign() {
        if (campaignId == null) return;

        Intent intent = new Intent(CampaignActivity.this, CampaignEditActivity.class);
        intent.putExtra("campaignId", campaignId);
        intent.putExtra("campaignName", title.getText().toString());
        intent.putExtra("campaignDescription", description.getText().toString());
        startActivity(intent);
    }

    private void loadCampaignDetails() {
        if (campaignId == null) return;

        firestore.collection("campaigns")
                .document(campaignId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        // Обрабатываем ошибки
                        Toast.makeText(CampaignActivity.this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Получаем обновленные данные
                        Campaign campaign = documentSnapshot.toObject(Campaign.class);
                        if (campaign != null) {
                            title.setText(campaign.getCampaignName());
                            description.setText(campaign.getCampaignDescription());
                            creatorId = campaign.getCreatorId();
                            String imageUrl = campaign.getImageURL();

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                if (!isDestroyed() && !isFinishing()) {
                                    Glide.with(CampaignActivity.this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.placeholder)
                                            .into(header_image2);
                                }
                            }

                            if (!auth.getCurrentUser().getUid().equals(creatorId)) {
                                findViewById(R.id.deletebtn).setVisibility(View.GONE);
                                findViewById(R.id.editbtn).setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }


    public void onDeleteCampaign() {
        if (campaignId == null) return;

        firestore.collection("campaigns")
                .document(campaignId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CampaignActivity.this, "Кампания удалена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CampaignActivity.this, "Ошибка при удалении кампании", Toast.LENGTH_SHORT).show();
                });
    }


}