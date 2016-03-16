package com.evdokimoveu.testvideoplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import java.io.IOException;
import java.util.ArrayList;

public class VideoPlayer extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl  {


    private MediaPlayer player;
    private MediaController controller;
    private ArrayList<String> channels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.video);
        SurfaceHolder videoHolder = surfaceView.getHolder();
        videoHolder.addCallback(this);

        Intent intent = getIntent();
        channels = intent.getStringArrayListExtra("channels");
        player = new MediaPlayer();
        controller = new MediaController(this);
        try{
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse("http://testapi.qix.sx/video/music.mp4"));
            player.setOnPreparedListener(this);
        } catch (IllegalArgumentException | IOException | SecurityException | IllegalStateException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        controller.show();
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        controller.setMediaPlayer(this);
        controller.setSurfaceView((FrameLayout) findViewById(R.id.video_container));
        player.start();
    }


    @Override
    public void start() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public void setVolume(float volume) {
        player.setVolume(volume, volume);
    }

    @Override
    public void showListChannels(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        for(int i = 0; i < channels.size(); i++){
            popupMenu.getMenu().add(channels.get(i));
        }
        popupMenu.show();
    }

}
