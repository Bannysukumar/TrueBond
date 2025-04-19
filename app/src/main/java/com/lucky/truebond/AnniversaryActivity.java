package com.lucky.truebond;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AnniversaryActivity extends AppCompatActivity {
    private TextInputEditText dateEditText;
    private TextView tvDaysTogether;
    private TextView tvNextAnniversary;
    private MaterialButton btnSave;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anniversary);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        dateEditText = findViewById(R.id.dateEditText);
        tvDaysTogether = findViewById(R.id.tvDaysTogether);
        tvNextAnniversary = findViewById(R.id.tvNextAnniversary);
        btnSave = findViewById(R.id.btnSave);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load existing anniversary date
        loadAnniversaryDate();

        // Setup date picker
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Setup save button
        btnSave.setOnClickListener(v -> saveAnniversaryDate());
    }

    private void loadAnniversaryDate() {
        mDatabase.child("users").child(currentUserId).child("anniversaryDate")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            long timestamp = snapshot.getValue(Long.class);
                            selectedDate.setTimeInMillis(timestamp);
                            updateUI();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AnniversaryActivity.this,
                                "Failed to load anniversary date", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateUI();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateUI() {
        // Update date display
        dateEditText.setText(dateFormat.format(selectedDate.getTime()));

        // Calculate days together
        long daysTogether = calculateDaysTogether();
        tvDaysTogether.setText(String.format("Days Together: %d", daysTogether));

        // Calculate next anniversary
        Calendar nextAnniversary = calculateNextAnniversary();
        tvNextAnniversary.setText(String.format("Next Anniversary: %s",
                dateFormat.format(nextAnniversary.getTime())));
    }

    private long calculateDaysTogether() {
        Calendar today = Calendar.getInstance();
        long diff = today.getTimeInMillis() - selectedDate.getTimeInMillis();
        return diff / (1000 * 60 * 60 * 24);
    }

    private Calendar calculateNextAnniversary() {
        Calendar today = Calendar.getInstance();
        Calendar nextAnniversary = (Calendar) selectedDate.clone();
        nextAnniversary.set(Calendar.YEAR, today.get(Calendar.YEAR));
        
        if (nextAnniversary.before(today)) {
            nextAnniversary.add(Calendar.YEAR, 1);
        }
        
        return nextAnniversary;
    }

    private void saveAnniversaryDate() {
        mDatabase.child("users").child(currentUserId).child("anniversaryDate")
                .setValue(selectedDate.getTimeInMillis())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AnniversaryActivity.this,
                                "Anniversary date saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AnniversaryActivity.this,
                                "Failed to save anniversary date", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 