package it.bugbuster.asilapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private Map<Integer, ExoPlayer> exoPlayerMap;

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
        holder.setTitle(videoList.get(position).getTitle());
        holder.exoPlayer.pause();

        exoPlayerMap.put(position, holder.exoPlayer);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public ExoPlayer getExoPlayerAtPosition(int position) {
        return exoPlayerMap.get(position);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private TextView videoTitle;
        private PlayerView playerView;
        private ExoPlayer exoPlayer;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            playerView = itemView.findViewById(R.id.playerView);
            exoPlayer = new ExoPlayer.Builder(context).build();
            playerView.setPlayer(exoPlayer);
        }

        public void setTitle(String title) {
            videoTitle.setText(title);
        }

        public void setVideo(String url) {
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(false);
        }
    }
}
