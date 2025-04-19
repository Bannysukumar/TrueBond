package com.lucky.truebond;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        // Set version name
        TextView versionText = findViewById(R.id.versionText);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("Version " + versionName);
        } catch (Exception e) {
            versionText.setText("Version 1.0");
        }

        // Setup contact button
        MaterialButton contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:truebond40@gmail.com"));
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });

        // Setup privacy policy button
        MaterialButton privacyButton = findViewById(R.id.privacyButton);
        privacyButton.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 