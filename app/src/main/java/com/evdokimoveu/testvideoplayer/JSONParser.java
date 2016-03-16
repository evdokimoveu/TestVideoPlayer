package com.evdokimoveu.testvideoplayer;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JSONParser extends AsyncTask<Void, Void, String> {

    private String resultJson = "";
    private ArrayList<String> titles;

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL("http://testapi.qix.sx/getAllTVChannels.json");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return resultJson;
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        titles = new ArrayList<>();

        try {
            JSONArray rootArray = new JSONArray(strJson);
            JSONObject itemsObj = rootArray.getJSONObject(0);
            JSONArray itemsArray = itemsObj.getJSONArray("items");
            for(int i = 0; i < itemsArray.length(); i++){
                JSONObject channel = itemsArray.getJSONObject(i);
                titles.add(channel.getString("title"));
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<String>getChannelTitles(){
        return titles;
    }
}
