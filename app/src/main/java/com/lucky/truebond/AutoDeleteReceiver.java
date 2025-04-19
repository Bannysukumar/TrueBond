package com.lucky.truebond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoDeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "DELETE_OLD_CHATS":
                    AutoDeleteManager.deleteOldChats();
                    break;
                case "DELETE_OLD_REPORTS":
                    AutoDeleteManager.deleteOldReports();
                    break;
            }
        }
    }
} 