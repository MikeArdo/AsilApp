package it.bugbuster.asilapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.RecyclerView;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.bugbuster.asilapp.entity.VideoModel;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoModel> videoList;
    private Context context;
    private Map<Integer, ExoPlayer> exoPlayerMap;  // Map to store ExoPlayer instances by position

    public VideoAdapter(Context context, List<VideoModel> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.exoPlayerMap = new HashMap<>();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.setVideo(videoList.get(position).getVideoUrl());
        holder.exoPlayer.pause();  // Ensure the player is paused when binding the view

        // Save the ExoPlayer instance in the map
        exoPlayerMap.put(position, holder.exoPlayer);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // Method to get ExoPlayer instance for a specific position
    public ExoPlayer getExoPlayerAtPosition(int position) {
        return exoPlayerMap.get(position);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private PlayerView playerView;
        private ExoPlayer exoPlayer;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            exoPlayer = new ExoPlayer.Builder(context).build();
            playerView.setPlayer(exoPlayer);
        }

        public void setVideo(String url) {
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(false);
        }
    }
}
