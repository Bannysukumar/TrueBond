package com.lucky.truebond;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LoveReportDialog extends DialogFragment {
    private final LoveReport loveReport;

    public LoveReportDialog(LoveReport loveReport) {
        this.loveReport = loveReport;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(R.layout.dialog_love_report);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            TextView textLoveScore = dialog.findViewById(R.id.textLoveScore);
            TextView textMood = dialog.findViewById(R.id.textMood);
            TextView textColdZones = dialog.findViewById(R.id.textColdZones);
            TextView textSuggestion = dialog.findViewById(R.id.textSuggestion);

            if (textLoveScore != null) textLoveScore.setText(String.valueOf(loveReport.getLoveScore()));
            if (textMood != null) textMood.setText(loveReport.getMood());
            if (textColdZones != null) textColdZones.setText(String.valueOf(loveReport.getColdZones()));
            if (textSuggestion != null) textSuggestion.setText(loveReport.getSuggestion());
        });

        return dialog;
    }
} 