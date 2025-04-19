package com.lucky.truebond;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.lucky.truebond.utils.ThemePreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private MaterialButton btnProfile;
    private MaterialButton btnAnniversary;
    private MaterialButton btnSingles;
    private MaterialButton btnNotifications;
    private MaterialButton btnPrivacy;
    private MaterialButton btnHelp;
    private MaterialButton btnAbout;
    private MaterialButton btnLogout;
    private SwitchMaterial switchTheme;
    private ThemePreferenceManager themePreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themePreferenceManager = new ThemePreferenceManager(this);
        initializeViews();
        setupToolbar();
        setupClickListeners();
        setupThemeSwitch();
    }

    private void initializeViews() {
        btnProfile = findViewById(R.id.buttonProfile);
        btnAnniversary = findViewById(R.id.buttonAnniversary);
        btnSingles = findViewById(R.id.buttonSingles);
        btnNotifications = findViewById(R.id.buttonNotifications);
        btnPrivacy = findViewById(R.id.buttonPrivacy);
        btnHelp = findViewById(R.id.buttonHelp);
        btnAbout = findViewById(R.id.buttonAbout);
        btnLogout = findViewById(R.id.buttonLogout);
        switchTheme = findViewById(R.id.switchTheme);
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupThemeSwitch() {
        switchTheme.setChecked(themePreferenceManager.isDarkMode());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themePreferenceManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate();
        });
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnAnniversary.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AnniversaryActivity.class);
            startActivity(intent);
        });

        btnSingles.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SinglesActivity.class);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        btnPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });

        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HelpSupportActivity.class);
            startActivity(intent);
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 