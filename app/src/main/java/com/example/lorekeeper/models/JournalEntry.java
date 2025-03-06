package com.example.lorekeeper.models;

import com.google.firebase.Timestamp;

public class JournalEntry {
    public String id, title, text, campaignId, creatorId;
    public Timestamp timestamp;
    public JournalEntry() {
    }

    public JournalEntry(String id, String campaignId, String title, String text, Timestamp timestamp, String creatorId) {
        this.id = id;
        this.campaignId = campaignId;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
        this.creatorId = creatorId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}


