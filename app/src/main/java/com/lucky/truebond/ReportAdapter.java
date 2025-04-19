package com.lucky.truebond;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private final List<LoveReportData> reports;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    public ReportAdapter(List<LoveReportData> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        LoveReportData report = reports.get(position);
        holder.textDate.setText(dateFormat.format(new Date(report.getTimestamp())));
        holder.textLoveScore.setText(String.format("Love Score: %d", report.getLoveScore()));
        holder.textMood.setText(String.format("Mood: %s", report.getMood()));
        holder.textColdZones.setText(String.format("Cold Zones: %d", report.getColdZones()));
        holder.textSuggestion.setText(report.getSuggestion());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textLoveScore;
        TextView textMood;
        TextView textColdZones;
        TextView textSuggestion;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textLoveScore = itemView.findViewById(R.id.textLoveScore);
            textMood = itemView.findViewById(R.id.textMood);
            textColdZones = itemView.findViewById(R.id.textColdZones);
            textSuggestion = itemView.findViewById(R.id.textSuggestion);
        }
    }
} 