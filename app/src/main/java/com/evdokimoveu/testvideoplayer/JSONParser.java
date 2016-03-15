package com.evdokimoveu.testvideoplayer;


import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JSONParser extends AsyncTask<Void, Void, String> {

    private HttpURLConnection urlConnection = null;
    private BufferedReader reader = null;
    private String resultJson = "";
    private List<String> titles;

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL("http://testapi.qix.sx/getAllTVChannels.json");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

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
        titles = new LinkedList<>();
        JSONObject dataJsonObj;
        String secondName = "";

        try {
            dataJsonObj = new JSONObject(strJson);
            JSONArray channels = dataJsonObj.getJSONArray("item");
            for (int i = 0; i < channels.length(); i++) {
                JSONObject channel = channels.getJSONObject(i);
                titles.add(channel.getString("title"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public List<String >getChannelTitles(){
        return titles;
    }
}
