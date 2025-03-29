package it.bugbuster.asilapp.entity;

public class VideoModel {
    private String title;
    private String videoUrl;

    public VideoModel(String title, String videoUrl) {
        this.title = title;
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}