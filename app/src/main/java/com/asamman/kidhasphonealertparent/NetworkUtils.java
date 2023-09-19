package com.asamman.kidhasphonealertparent;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    // Function to send a POST request with the provided JSON data
    public static String sendPostRequest(String apiUrl, String jsonData) {
        HttpURLConnection urlConnection = null;
        String jsonResponse = null;

        try {
            // Create the URL object
            URL url = new URL(apiUrl);

            // Open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            urlConnection.setDoOutput(true);

            // Write the JSON data to the request
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(jsonData.getBytes());
            out.flush();
            out.close();

            // Get the response
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                InputStream errorStream = urlConnection.getErrorStream();
                if (errorStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Log.d("NetworkUtils", line);
                        }
                    }
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                jsonResponse = NetworkUtils.readStream(urlConnection.getInputStream());
            } else {
                jsonResponse = "Error: HTTP response code " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return jsonResponse.trim();
    }

    // Function to read the input stream and convert it to a String
    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        reader.close();
        return stringBuilder.toString();
    }
}
