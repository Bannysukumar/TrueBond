package com.lucky.truebond;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private LineChart chartLoveScore;
    private BarChart chartMood;
    private PieChart chartColdZones;
    private RecyclerView recyclerViewReports;
    private ReportAdapter reportAdapter;
    private List<LoveReportData> reports;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String partnerId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Get partner ID from intent or database
        partnerId = getIntent().getStringExtra("partnerId");
        if (partnerId == null) {
            loadPartnerId();
        } else {
            setupHistory();
        }

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Love Report History");

        // Initialize views
        chartLoveScore = findViewById(R.id.chartLoveScore);
        chartMood = findViewById(R.id.chartMood);
        chartColdZones = findViewById(R.id.chartColdZones);
        recyclerViewReports = findViewById(R.id.recyclerViewReports);

        // Setup RecyclerView
        reports = new ArrayList<>();
        reportAdapter = new ReportAdapter(reports);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReports.setAdapter(reportAdapter);
    }

    private void loadPartnerId() {
        mDatabase.child("users").child(currentUserId).child("partnerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            partnerId = snapshot.getValue(String.class);
                            setupHistory();
                        } else {
                            Toast.makeText(HistoryActivity.this, "No partner connected", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, "Failed to load partner", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupHistory() {
        if (currentUserId == null || partnerId == null) {
            Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chat ID by sorting user IDs
        chatId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        // Load reports
        loadReports();
    }

    private void loadReports() {
        if (chatId == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long cutoffTime = calendar.getTimeInMillis();

        mDatabase.child("reports").child(chatId)
                .orderByChild("timestamp")
                .startAt(cutoffTime)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reports.clear();
                        for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                            LoveReportData report = reportSnapshot.getValue(LoveReportData.class);
                            if (report != null) {
                                report.setId(reportSnapshot.getKey());
                                reports.add(report);
                            }
                        }
                        reportAdapter.notifyDataSetChanged();
                        updateCharts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCharts() {
        if (reports.isEmpty()) {
            return;
        }

        // Update Love Score Chart
        List<Entry> loveScoreEntries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

        for (int i = 0; i < reports.size(); i++) {
            LoveReportData report = reports.get(i);
            loveScoreEntries.add(new Entry(i, report.getLoveScore()));
            dates.add(dateFormat.format(new Date(report.getTimestamp())));
        }

        LineDataSet loveScoreDataSet = new LineDataSet(loveScoreEntries, "Love Score");
        loveScoreDataSet.setColor(Color.RED);
        loveScoreDataSet.setValueTextColor(Color.BLACK);
        loveScoreDataSet.setValueTextSize(12f);

        LineData loveScoreData = new LineData(loveScoreDataSet);
        chartLoveScore.setData(loveScoreData);
        chartLoveScore.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        chartLoveScore.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartLoveScore.getDescription().setEnabled(false);
        chartLoveScore.invalidate();

        // Update Mood Chart
        List<BarEntry> moodEntries = new ArrayList<>();
        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (LoveReportData report : reports) {
            switch (report.getMood()) {
                case "POSITIVE":
                    positiveCount++;
                    break;
                case "NEGATIVE":
                    negativeCount++;
                    break;
                default:
                    neutralCount++;
            }
        }

        moodEntries.add(new BarEntry(0, positiveCount));
        moodEntries.add(new BarEntry(1, negativeCount));
        moodEntries.add(new BarEntry(2, neutralCount));

        BarDataSet moodDataSet = new BarDataSet(moodEntries, "Mood Distribution");
        moodDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        moodDataSet.setValueTextColor(Color.BLACK);
        moodDataSet.setValueTextSize(12f);

        BarData moodData = new BarData(moodDataSet);
        chartMood.setData(moodData);
        chartMood.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Positive", "Negative", "Neutral"}));
        chartMood.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartMood.getDescription().setEnabled(false);
        chartMood.invalidate();

        // Update Cold Zones Chart
        List<PieEntry> coldZoneEntries = new ArrayList<>();
        float totalColdZones = 0;
        for (LoveReportData report : reports) {
            totalColdZones += report.getColdZones();
        }
        if (totalColdZones > 0) {
            coldZoneEntries.add(new PieEntry(totalColdZones, "Cold Zones"));
            coldZoneEntries.add(new PieEntry(reports.size() - totalColdZones, "Normal Zones"));
        }

        PieDataSet coldZoneDataSet = new PieDataSet(coldZoneEntries, "Communication Zones");
        coldZoneDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        coldZoneDataSet.setValueTextColor(Color.BLACK);
        coldZoneDataSet.setValueTextSize(12f);

        PieData coldZoneData = new PieData(coldZoneDataSet);
        chartColdZones.setData(coldZoneData);
        chartColdZones.getDescription().setEnabled(false);
        chartColdZones.invalidate();
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