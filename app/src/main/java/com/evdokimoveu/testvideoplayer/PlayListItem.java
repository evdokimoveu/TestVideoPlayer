package com.evdokimoveu.testvideoplayer;


public class PlayListItem {
    private String nameVideo;
    private String urlVideo;

    public PlayListItem(String nameVideo, String urlVideo) {
        this.nameVideo = nameVideo;
        this.urlVideo = urlVideo;
    }

    public String getNameVideo() {
        return nameVideo;
    }

    public void setNameVideo(String nameVideo) {
        this.nameVideo = nameVideo;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    @Override
    public String toString() {
        return "PlayListItem{" +
                "nameVideo='" + nameVideo + '\'' +
                ", urlVideo='" + urlVideo + '\'' +
                '}';
    }
}
