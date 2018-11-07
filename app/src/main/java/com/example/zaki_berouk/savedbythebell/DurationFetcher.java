package com.example.zaki_berouk.savedbythebell;


import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class DurationFetcher extends AsyncTask<String, Void, Long> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Long doInBackground(String... params) {
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?key=" + params[0];
        urlString += "&origin=" + params[1];
        urlString += "&destination=" + params[2].replace(" ", "+");
        urlString += "&departure_time=" + params[3];

        Log.d("DurationFetcher", "doInBackground: " + urlString);

        URLConnection urlConn = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(urlString);
            Log.d("App", "doInBackground: " + url);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            try{
                JSONObject jsonObject = new JSONObject(stringBuffer.toString()).getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                Long result = jsonObject.getJSONObject("duration_in_traffic").getLong("value");
                return result;
            } catch (Exception e){
                return Long.valueOf(1);
            }

        } catch (Exception ex) {
            Log.e("App", "DurationFetcher", ex);
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}