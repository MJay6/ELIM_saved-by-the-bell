package com.example.zaki_berouk.savedbythebell;


import android.content.res.AssetManager;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;

import com.example.zaki_berouk.savedbythebell.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class GetEvents extends AsyncTask<String, Void, ArrayList<Event>> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected ArrayList<Event> doInBackground(String... params) {
        String urlString = "http://192.168.43.65:1880/elim";

        Log.d("GetEvents", "doInBackground: " + urlString);

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
                JSONArray jsonArray = new JSONArray(stringBuffer.toString());
                ArrayList<Event> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Log.i("for",jsonArray.getJSONObject(i).getString("name"));

                    result.add(new Event(jsonArray.getJSONObject(i).getString("name"),
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(jsonArray.getJSONObject(i).getString("date")),
                            jsonArray.getJSONObject(i).getString("location"),
                            jsonArray.getJSONObject(i).getString("description"),
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(jsonArray.getJSONObject(i).getString("departureTime"))
                            ));
                }
                return result;
            } catch (Exception e){
                return null;
            }

        } catch (Exception ex) {
            Log.e("App", "GetEvents", ex);
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