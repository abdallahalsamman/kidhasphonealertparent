package com.asamman.kidhasphonealertparent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.util.Log;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void startTimer() {
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
//        }, 0, 3 * 60 * 1000);
        }, 0, 3 * 60 * 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void notify(String kidName) {
        int seconds = 1;
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_MESSAGE, kidName)
                .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this intent!");
        }
    }

    private void fetchAndNotify() throws JSONException {
        // Fetch data from Firestore
        String jsonResponse = fetchDataFromFirestore(retrieveStrings());

        compareAndNotify(jsonResponse);
    }

    private void compareAndNotify(String jsonResponse) {
        if (jsonResponse == null) {
            return;
        }

        try {
            JSONArray documents = new JSONArray(jsonResponse);

            if (documents.length() > 0) {
                JSONObject document = documents.getJSONObject(0).getJSONObject("document");
                JSONObject fields = document.getJSONObject("fields");
                String kidName = fields.getJSONObject("kid_name").getString("stringValue");
                int integerValue = fields.getJSONObject("timestamp").getInt("integerValue");

                SharedPreferences sharedPreferences = getSharedPreferences("com.asamman.kidhasphonealertparent", MODE_PRIVATE);
                int lastFetchedIntegerValue = sharedPreferences.getInt(kidName, 0);

                if (integerValue > lastFetchedIntegerValue) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(kidName, integerValue);
                    editor.apply();

                    notify(kidName);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
        }
    }



    private String fetchDataFromFirestore(String[] kids) throws JSONException {
        String apiUrl = "https://firestore.googleapis.com/v1/projects/kidhasphonealert/databases/(default)/documents:runQuery?key=AIzaSyAYGBPOO1kPiPceMuUa_BQzWhyV92-sNas";
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
