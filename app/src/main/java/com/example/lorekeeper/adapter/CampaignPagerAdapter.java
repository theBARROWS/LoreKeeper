package com.example.lorekeeper.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lorekeeper.fragments.CampaignCharactersFragment;
import com.example.lorekeeper.fragments.CampaignJournalFragment;

public class CampaignPagerAdapter extends FragmentStateAdapter {
    private String campaignId;
    public CampaignPagerAdapter(@NonNull FragmentActivity fragmentActivity, String campaignId) {
        super(fragmentActivity);
        this.campaignId = campaignId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = CampaignJournalFragment.newInstance(campaignId);
                break;
            case 1:
                fragment = CampaignCharactersFragment.newInstance(campaignId);
                break;
            default:
                fragment = CampaignJournalFragment.newInstance(campaignId);
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
