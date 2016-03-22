package com.evdokimoveu.testvideoplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;

public class VideoPlayer extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl  {

    private MediaPlayer player;
    private MediaController controller;
    private ArrayList<String> channels;
    private ArrayList<PlayListItem> playList;
    private boolean opening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.video);
        SurfaceHolder videoHolder = surfaceView.getHolder();
        videoHolder.addCallback(this);

        this.opening = false;

        Intent intent = getIntent();
        channels = intent.getStringArrayListExtra("channels");

        //Add video to play list
        playList = new ArrayList<>();
        playList.add(new PlayListItem("Клип", "http://testapi.qix.sx/video/music.mp4"));
        playList.add(new PlayListItem("Трейлер", "http://testapi.qix.sx/video/trailer.mp4"));

        String url = "http://testapi.qix.sx/video/music.mp4";

        player = new MediaPlayer();
        controller = new MediaController(this);
        try{
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(url));
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
        try{
            player.setDisplay(holder);
            player.prepareAsync();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.release();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.v(VideoPlayer.class.getName(), "onPrepared()");
        controller.setMediaPlayer(this);
        controller.setSurfaceView((FrameLayout) findViewById(R.id.video_container));
        opening = false;
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
    public ArrayList<String> getChannels() {
        return channels;
    }

    @Override
    public ArrayList<PlayListItem> getPlayList() {
        return playList;
    }

    @Override
    public boolean isOpening() {
        return opening;
    }

    @Override
    public void playNewVideo(String url) {
        try {
            player.reset();
            opening = true;
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(url));
            player.prepareAsync();
            player.setOnPreparedListener(this);
        } catch (IllegalArgumentException | IOException | SecurityException | IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
