package com.lucky.truebond;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private MaterialButton btnNext, btnSkip;
    private OnboardingAdapter onboardingAdapter;
    private List<OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        // Setup onboarding items
        setupOnboardingItems();
        setupIndicators();
        setCurrentIndicator(0);

        // Setup ViewPager2
        onboardingAdapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(onboardingAdapter);

        // ViewPager2 page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started");
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("Next");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        // Button click listeners
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
        });

        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_connect,
                "Connect with Your Partner",
                "Generate a unique code or scan QR to connect with your partner"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_anniversary,
                "Track Your Special Days",
                "Never miss important dates with anniversary tracking and reminders"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_notifications,
                "Stay Connected",
                "Get notifications for important events and partner activities"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_privacy,
                "Privacy First",
                "Your data is secure with our privacy-focused approach"
        ));
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[onboardingItems.size()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            indicatorLayout.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorLayout.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_inactive
                ));
            }
        }
    }
} 