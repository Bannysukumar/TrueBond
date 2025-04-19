package com.lucky.truebond;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup email support button
        MaterialButton btnEmailSupport = findViewById(R.id.btnEmailSupport);
        btnEmailSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:truebond40@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TrueBond Support Request");
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        });

        // Setup live chat button
        MaterialButton btnLiveChat = findViewById(R.id.btnLiveChat);
        btnLiveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement live chat functionality
                // For now, show a message
                android.widget.Toast.makeText(HelpSupportActivity.this, 
                    "Live chat coming soon!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 