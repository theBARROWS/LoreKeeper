package com.example.lorekeeper;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.lorekeeper.fragments.CampaignFragment;
import com.example.lorekeeper.fragments.CharacterFragment;
import com.example.lorekeeper.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoreKeeperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lore_keeper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Fragment firstFragment = new CampaignFragment();
        Fragment secondFragment = new CharacterFragment();
        Fragment thirdFragment = new ProfileFragment();

        setCurrentFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.campaigns) {
                selectedFragment = new CampaignFragment();
            } else if (itemId == R.id.characters) {
                selectedFragment = new CharacterFragment();
            } else if (itemId == R.id.profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                setCurrentFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}