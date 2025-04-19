package com.lucky.truebond;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AutoDeleteManager {
    private static final long CHAT_DELETION_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    private static final long REPORT_DELETION_INTERVAL = 7 * 24 * 60 * 60 * 1000; // 7 days

    public static void scheduleAutoDelete(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Schedule chat deletion
        Intent chatIntent = new Intent(context, AutoDeleteReceiver.class);
        chatIntent.setAction("DELETE_OLD_CHATS");
        PendingIntent chatPendingIntent = PendingIntent.getBroadcast(
            context, 0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        // Schedule report deletion
        Intent reportIntent = new Intent(context, AutoDeleteReceiver.class);
        reportIntent.setAction("DELETE_OLD_REPORTS");
        PendingIntent reportPendingIntent = PendingIntent.getBroadcast(
            context, 1, reportIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set repeating alarms
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + CHAT_DELETION_INTERVAL,
            CHAT_DELETION_INTERVAL,
            chatPendingIntent
        );

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + REPORT_DELETION_INTERVAL,
            REPORT_DELETION_INTERVAL,
            reportPendingIntent
        );
    }

    public static void deleteOldChats() {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        long cutoffTime = System.currentTimeMillis() - CHAT_DELETION_INTERVAL;
        
        Query oldChatsQuery = chatsRef.orderByChild("timestamp").endAt(cutoffTime);
        oldChatsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    chatSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public static void deleteOldReports() {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        long cutoffTime = System.currentTimeMillis() - REPORT_DELETION_INTERVAL;
        
        Query oldReportsQuery = reportsRef.orderByChild("timestamp").endAt(cutoffTime);
        oldReportsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    reportSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
} 