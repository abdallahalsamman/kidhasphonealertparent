package com.asamman.kidhasphonealertparent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private Timer timer;
    private String TAG = "BackgroundService";
    private MediaPlayer mediaPlayer;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        String channelId = createNotificationChannel("ForeGroundService", "My Background Service");
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Service Running")
                .setContentText("This is a foreground service notification.")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(10, notification);

        startTimer();

        mediaPlayer = new MediaPlayer();
        handler = new Handler();

        Log.d(TAG, "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra("command");
        if (command != null && command.equals("notify")) {
            notify("TEST KID");
        } else if (command != null && command.equals("STOP_RINGTONE")) {
            stopRingtone();
//            notificationManager.cancelAll();
        }

        return START_STICKY;
    }

    private String createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Your service channel description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return channelId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    fetchAndNotify();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 10 * 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private NotificationManager notificationManager;
    public void notify(String kidName) {
        // Play alarm default alarm sound
        playDefaultAlarmSound();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "KidHasAlertChannel",
                    "KidHasAlertChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("KidHasPhoneAlert channel for foreground service notification");
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Intent clickIntent = new Intent(this, BootReceiver.class);
            clickIntent.setAction("NOTIFICATION_CLICKED");
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent cancelIntent = new Intent(this, BootReceiver.class);
            cancelIntent.setAction("NOTIFICATION_CLICKED");
            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "KidHasAlertChannel")
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setContentTitle("KidHasPhoneAlert")
                    .setContentText(kidName + " has phone")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(clickPendingIntent) // Set the click intent
                    .setDeleteIntent(cancelPendingIntent) // Set the delete intent
                    .setAutoCancel(true);

            notificationManager.notify(1, builder.build());
        }
    }

    private void fetchAndNotify() throws JSONException {
        // Fetch data from Firestore
        String jsonResponse = fetchDataFromFirestore(retrieveStrings());

        compareAndNotify(jsonResponse);
    }

    private Ringtone ringtone;

    private void stopRingtone() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private void playDefaultAlarmSound() {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                // If alarm sound is not available, use notification sound
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setLooping(true);
            }

            if (ringtone.isPlaying()) {
                ringtone.stop();
            }

            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        mediaPlayer.stop();
    }

    private void compareAndNotify(String jsonResponse) {
        if (jsonResponse == null) {
            return;
        }

        try {
            JSONArray documents = new JSONArray(jsonResponse);

            for (int i = 0; documents.length() > i; i++) {
                JSONObject document = documents.getJSONObject(i).getJSONObject("document");
                JSONObject fields = document.getJSONObject("fields");
                String kidName = fields.getJSONObject("kid_name").getString("stringValue");
                long integerValue = fields.getJSONObject("timestamp").getLong("integerValue");

                SharedPreferences sharedPreferences = getSharedPreferences("com.asamman.kidhasphonealertparent", MODE_PRIVATE);
                long lastFetchedIntegerValue = sharedPreferences.getLong(kidName, 0L);

                if (integerValue > lastFetchedIntegerValue) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(kidName, integerValue);
                    editor.apply();

                    Log.i(TAG, "New alert for " + kidName + "\n" + "Last fetched: " + lastFetchedIntegerValue + "\n" + "Current: " + integerValue);
                    notify(kidName);
                } else {
                    Log.d(TAG, "No new alerts for " + kidName + "\n" + "Last fetched: " + lastFetchedIntegerValue + "\n" + "Current: " + integerValue);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
        }
    }



    private String fetchDataFromFirestore(String[] kids) throws JSONException {
        String apiUrl = "https://firestore.googleapis.com/v1/projects/kidhasphonealert/databases/(default)/documents:runQuery?key=AIzaSyAYGBPOO1kPiPceMuUa_BQzWhyV92-sNas";
        if (kids.length == 0) {
            return null;
        }
        String firestoreQuery = FirestoreQuery.generateQueryJson(kids);

        String responseJson = NetworkUtils.sendPostRequest(apiUrl, firestoreQuery);
        return responseJson;
    }

    private String[] retrieveStrings() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.asamman.kidhasphonealertparent", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        String[] stringArray = new String[allEntries.size()];

        int index = 0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            stringArray[index++] = entry.getKey();
        }

        return stringArray;
    }
}
