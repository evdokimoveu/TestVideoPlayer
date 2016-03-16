package com.evdokimoveu.testvideoplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final JSONParser parser= new JSONParser();
        parser.execute();

        Thread welcomeThread = new Thread(){
            @Override
            public void run() {
                try {
                    do {
                        sleep(3000);
                        ArrayList<String> channels = parser.getChannelTitles();
                        Intent intent = new Intent(Welcome.this, VideoPlayer.class);
                        intent.putStringArrayListExtra("channels", channels);
                        startActivity(intent);
                    } while (!parser.getStatus().equals(AsyncTask.Status.FINISHED));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}
