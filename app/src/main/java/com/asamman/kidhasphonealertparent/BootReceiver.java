package com.asamman.kidhasphonealertparent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            Log.i("BOOTRECEIVER", "Intent action is null");
            return;
        }

        Log.i("BOOTRECEIVER", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent myIntent = new Intent(context, BackgroundService.class);
            context.startService(myIntent);
        } else if (intent.getAction().equals("NOTIFICATION_CLICKED")) {
            Log.i("BOOTRECEIVER", "NOTIFICATION CLICKED");
            Intent myIntent = new Intent(context, BackgroundService.class);
            myIntent.putExtra("command", "STOP_RINGTONE");
            context.startService(myIntent);
        }
    }
}
