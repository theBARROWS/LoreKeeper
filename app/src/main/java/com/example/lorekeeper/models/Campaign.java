package com.example.lorekeeper.models;

import com.google.firebase.database.Exclude;

import java.util.List;

public class Campaign {
    public String id, campaignName, campaignDescription, creatorId, imageURL;
    @Exclude
    private boolean isEditable;
    public List<String> characterIds;

    public Campaign() {
    }

    public Campaign(String id, String campaignName, String campaignDescription, String imageURL, String creatorId, List<String> characterIds) {
        this.id = id;
        this.campaignName = campaignName;
        this.campaignDescription = campaignDescription;
        this.creatorId = creatorId;
        this.imageURL = imageURL;
        this.characterIds = characterIds;
    }

//    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCampaignDescription() {
        return campaignDescription;
    }

    public void setCampaignDescription(String campaignDescription) {
        this.campaignDescription = campaignDescription;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
    public boolean isEditable() {
        return isEditable;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }
}
