package com.example.lorekeeper.adapter;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraOfflineSession;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lorekeeper.CampaignActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.models.Campaign;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class campaignAdapter extends RecyclerView.Adapter<campaignAdapter.CampaignViewHolder> {
    private List<Campaign> campaignList;
    private List<Campaign> filteredList;
    private Context context;

    public campaignAdapter(List<Campaign> campaignList, Context context){
        this.campaignList = campaignList;
        this.filteredList = new ArrayList<>(campaignList);
        this.context = context;
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_campaign, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = filteredList.get(position);
        holder.title.setText(campaign.getCampaignName());
        holder.subhead.setText(campaign.getCampaignDescription());

        if (campaign.isEditable()) {
            holder.creatorIcon.setVisibility(View.VISIBLE);
        } else {
            holder.creatorIcon.setVisibility(View.GONE);
        }
        String imageUrl = campaign.getImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.header_image);
        }

            holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CampaignActivity.class);
            intent.putExtra("campaignId", campaign.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(campaignList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Campaign campaign : campaignList) {
                if (campaign.getCampaignName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(campaign);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void updateData(List<Campaign> newCampaignList) {
        campaignList.clear();
        campaignList.addAll(newCampaignList);
        filter("");
    }

    public static class CampaignViewHolder extends RecyclerView.ViewHolder {
//        ImageView headerImage;
        TextView title, subhead;
        ImageView creatorIcon, header_image;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subhead = itemView.findViewById(R.id.subhead);
            creatorIcon = itemView.findViewById(R.id.ic_creator);
            header_image = itemView.findViewById(R.id.header_image);
        }
    }
}

