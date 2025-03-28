package it.bugbuster.asilapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.entity.VideoModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InformationFragment extends Fragment {

    private ViewPager2 videoViewPager;
    private ExoPlayer exoPlayer;
    private VideoAdapter videoAdapter;
    private List<VideoModel> videoList;
    private TabLayout tabLayout;
    private int previousPosition = -1;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InformationFragment newInstance(String param1, String param2) {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);
        videoViewPager = view.findViewById(R.id.videoViewPager);
        videoList = new ArrayList<>();
        tabLayout = view.findViewById(R.id.tabLayout);

        // Add sample video URLs
        videoList.add(new VideoModel("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"));
        videoList.add(new VideoModel("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"));
        videoList.add(new VideoModel("https://www.w3schools.com/html/mov_bbb.mp4"));

        videoAdapter = new VideoAdapter(requireContext(), videoList);

        videoViewPager.setAdapter(videoAdapter);

        new TabLayoutMediator(tabLayout, videoViewPager, (tab, position) -> {

        }).attach();

        videoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                exoPlayer = videoAdapter.getExoPlayerAtPosition(previousPosition);
                // Pause the video when switching pages
                if (exoPlayer != null) {
                    exoPlayer.pause();
                }
                previousPosition = position;
            }

        });

        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        exoPlayer = videoAdapter.getExoPlayerAtPosition(previousPosition);
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        exoPlayer = videoAdapter.getExoPlayerAtPosition(previousPosition);
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        exoPlayer = videoAdapter.getExoPlayerAtPosition(previousPosition);
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

}