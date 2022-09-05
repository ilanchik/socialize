package com.ilan.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;
import com.ilan.socialize.databinding.ActivityMainBinding;
import com.ilan.socialize.fragments.HomeFragment;
import com.ilan.socialize.fragments.NotificationsFragment;
import com.ilan.socialize.fragments.ProfileFragment;
import com.ilan.socialize.fragments.SearchFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Replace fragment according to selected bottom navigation
        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;

                    case R.id.nav_search:
                        selectedFragment = new SearchFragment();
                        break;

                    case R.id.nav_add:
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, PostsActivity.class));
                        break;

                    case R.id.nav_likes:
                        selectedFragment = new NotificationsFragment();
                        break;

                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });

        // Pass information from activity to fragment
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String profileId = intent.getString("publisherId");

            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
            binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        } else {
            // Start with Home fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }
}