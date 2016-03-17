package com.evdokimoveu.testvideoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MediaController extends FrameLayout {

    private View viewMediaController;
    private Context mediaControllerContext;
    private ViewGroup viewGroup;
    private MediaPlayerControl playerControl;
    private ProgressBar progressBar;
    private TextView videoName;
    private final static int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int MUSIC = 0;
    private static final int TRAILER = 1;
    private ImageButton tvButton;
    private ImageButton playListButton;
    private ImageButton volumeButton;
    private ImageButton playButton;
    private ImageButton forwardButton;
    private ImageButton backwardButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private boolean isShow;
    private boolean dragging;
    private boolean isMute;
    private boolean isNext;
    private boolean isPrev;
    private ArrayList<PlayListItem> playListItems;

    private Handler handler = new MessageHandler(this);


    public MediaController(Context context) {
        super(context);
        this.mediaControllerContext = context;
        this.isMute = false;
        this.isNext = true;
        this.isPrev = false;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (viewMediaController != null)
            initMediaControllerView(viewMediaController);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        playerControl = player;
        playListItems = playerControl.getPlayList();
        updatePlayButton();
    }

    public void setSurfaceView(ViewGroup viewGroup){
        this.viewGroup = viewGroup;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = setMediaControllerView();
        addView(v, frameParams);
    }

    protected View setMediaControllerView(){
        LayoutInflater inflater = (LayoutInflater)mediaControllerContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewMediaController = inflater.inflate(R.layout.media_controller, null);
        initMediaControllerView(viewMediaController);
        return viewMediaController;
    }

    @SuppressLint("WrongViewCast")
    private void initMediaControllerView(View v){
        tvButton = (ImageButton)v.findViewById(R.id.tv);
        if(tvButton != null){
            tvButton.setOnClickListener(tvButtonListener);
        }

        playListButton = (ImageButton)v.findViewById(R.id.play_list);
        if(playListButton != null){
            playListButton.setOnClickListener(playListButtonListener);
        }

        volumeButton = (ImageButton)v.findViewById(R.id.volume);
        if(volumeButton != null){
            volumeButton.setOnClickListener(volumeButtonListener);
        }

        playButton = (ImageButton)v.findViewById(R.id.play);
        if(playButton != null){
            playButton.setOnClickListener(playButtonListener);
        }

        forwardButton = (ImageButton)v.findViewById(R.id.forward);
        if(forwardButton != null){
            forwardButton.setOnClickListener(forwardButtonListener);
        }

        backwardButton = (ImageButton)v.findViewById(R.id.backward);
        if(backwardButton != null){
            backwardButton.setOnClickListener(backwardButtonListener);
        }

        previousButton = (ImageButton)v.findViewById(R.id.previous);
        if(previousButton != null){
            previousButton.setOnClickListener(previousButtonListener);
            if(!isPrev){
                previousButton.setClickable(false);
            }
        }

        nextButton = (ImageButton)v.findViewById(R.id.next);
        if(nextButton != null){
            nextButton.setOnClickListener(nextButtonListener);
            if(!isNext){
                nextButton.setClickable(false);
            }
        }

        videoName = (TextView)v.findViewById(R.id.video_name);
        if(playListItems.size() > 0){
            videoName.setText(playListItems.get(0).getNameVideo());
        }

        progressBar = (ProgressBar)v.findViewById(R.id.seek_bar);
        if (progressBar != null) {
            if (progressBar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) progressBar;
                seeker.setOnSeekBarChangeListener(seekListener);
            }
            progressBar.setMax(1000);
        }
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    private void disableUnsupportedButtons() {
        if (playerControl == null) {
            return;
        }

        try {
            if (playButton != null && !playerControl.canPause()) {
                playButton.setEnabled(false);
            }
            if (backwardButton != null && !playerControl.canSeekBackward()) {
                backwardButton.setEnabled(false);
            }
            if (forwardButton != null && !playerControl.canSeekForward()) {
                forwardButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            ex.printStackTrace();
        }
    }

    public void show(int timeout) {
        if (!isShow && viewGroup != null) {
            setProgress();
            if (playButton != null) {
                playButton.requestFocus();
            }
            disableUnsupportedButtons();

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            viewGroup.addView(this, tlp);
            isShow = true;
        }
        updatePlayButton();

        handler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = handler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, timeout);
        }
    }
    public boolean isShowing(){
        return isShow;
    }

    public void hide() {
        if (viewGroup == null) {
            return;
        }

        try {
            viewGroup.removeView(this);
            handler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        isShow = false;
    }

    private int setProgress() {
        if (playerControl == null || dragging) {
            return 0;
        }

        int position = playerControl.getCurrentPosition();
        int duration = playerControl.getDuration();
        if (progressBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                progressBar.setProgress((int) pos);
            }
            int percent = playerControl.getBufferPercentage();
            progressBar.setSecondaryProgress(percent * 10);
        }

        return position;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(DEFAULT_TIMEOUT);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (playerControl == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPlayResume();
                show(DEFAULT_TIMEOUT);
                if (playButton != null) {
                    playButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !playerControl.isPlaying()) {
                playerControl.start();
                updatePlayButton();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && playerControl.isPlaying()) {
                playerControl.pause();
                updatePlayButton();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    private View.OnClickListener tvButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(mediaControllerContext, tvButton);
            ArrayList<String> channels = playerControl.getChanels();
            for(int i = 0; i < channels.size(); i++){
                popupMenu.getMenu().add(channels.get(i));
            }
            popupMenu.show();
        }
    };

    private View.OnClickListener playListButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(mediaControllerContext, playListButton);
            for(int i = 0; i < playListItems.size(); i++){
                popupMenu.getMenu().add(0, i, 0, playListItems.get(i).getNameVideo());            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String url;
                    switch (item.getItemId()){
                        case MUSIC:
                            url = playListItems.get(MUSIC).getUrlVideo();
                            playerControl.playNewVideo(url);
                            videoName.setText(playListItems.get(MUSIC).getNameVideo());
                            return true;
                        case TRAILER:
                            url = playListItems.get(TRAILER).getUrlVideo();
                            playerControl.playNewVideo(url);
                            videoName.setText(playListItems.get(TRAILER).getNameVideo());
                            return true;
                        default:
                            return true;
                    }
                }
            });
            popupMenu.show();
        }
    };

    private View.OnClickListener volumeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isMute){
                playerControl.setVolume(1);
                isMute = false;
                updateVolumeButton();
            }
            else{
                playerControl.setVolume(0);
                isMute = true;
                updateVolumeButton();
            }
        }
    };
    private View.OnClickListener playButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPlayResume();
            show(DEFAULT_TIMEOUT);
        }
    };
    private View.OnClickListener forwardButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (playerControl == null) {
                return;
            }
            int pos = playerControl.getCurrentPosition();
            pos += 15000; // milliseconds
            playerControl.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    private View.OnClickListener backwardButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (playerControl == null) {
                return;
            }
            int pos = playerControl.getCurrentPosition();
            pos -= 5000; // milliseconds
            playerControl.seekTo(pos);
            setProgress();
            show(DEFAULT_TIMEOUT);
        }
    };

    private View.OnClickListener previousButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener nextButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            dragging = true;
            handler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (playerControl == null) {
                return;
            }
            if (!fromuser) {
                return;
            }

            long duration = playerControl.getDuration();
            long newposition = (duration * progress) / 1000L;
            playerControl.seekTo( (int) newposition);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            dragging = false;
            setProgress();
            updatePlayButton();
            show(DEFAULT_TIMEOUT);
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private void updatePlayButton() {
        if (viewMediaController == null || playButton == null || playerControl == null) {
            return;
        }
        if (playerControl.isPlaying()) {
            playButton.setImageResource(R.drawable.pause_circle_outline);
        } else {
            playButton.setImageResource(R.drawable.play_circle_outline);
        }
    }

    private void updateVolumeButton(){
        if (viewMediaController == null || volumeButton == null || playerControl == null) {
            return;
        }
        if(isMute){
            volumeButton.setImageResource(R.drawable.volume_off);
        }
        else{
            volumeButton.setImageResource(R.drawable.volume_high);
        }
    }

    private void checkNextPrev(int index, int sizeList){
        if(sizeList > 0){
            if(index == 0){
                isPrev = false;
                isNext = true;
            }
            else{
                isPrev = true;
                if(index == sizeList - 1){
                    isNext = false;
                }
                else{
                    isNext = true;
                }
            }
            updateNextButton();
            updatePrevButton();
        }
        else{
            Log.w("MediaController", "Playlist is empty!");
        }
    }

    private void updatePrevButton(){
        if (viewMediaController == null ||
                previousButton == null ||
                playerControl == null) {
            return;
        }
        if(isPrev){
            previousButton.setClickable(true);
        }
        else{
            previousButton.setClickable(false);
        }
    }

    private void updateNextButton(){
        if (viewMediaController == null ||
                nextButton == null ||
                playerControl == null) {
            return;
        }
        if(isNext){
            nextButton.setClickable(true);
        }
        else{
            nextButton.setClickable(false);
        }
    }

    private void doPlayResume() {
        if (playerControl == null) {
            return;
        }

        if (playerControl.isPlaying()) {
            playerControl.pause();
        } else {
            playerControl.start();
        }
        updatePlayButton();
    }

    public interface MediaPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();
        void    setVolume(float volume);
        void    playNewVideo(String url);
        ArrayList<String> getChanels();
        ArrayList<PlayListItem> getPlayList();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<MediaController> mView;

        MessageHandler(MediaController view) {
            mView = new WeakReference<>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            MediaController view = mView.get();
            if (view == null || view.playerControl == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.dragging && view.isShow && view.playerControl.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}
