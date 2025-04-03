package it.bugbuster.asilapp.information;

import static it.bugbuster.asilapp.AnimationFragment.setFragmentAnimation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.DocumentAndContactDialog;
import it.bugbuster.asilapp.MapsFragment;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.adapter.VideoAdapter;
import it.bugbuster.asilapp.entity.VideoModel;
import it.bugbuster.asilapp.refugee_shelter.RefugeeShelterFragment;
import it.bugbuster.asilapp.utils.NavigationUtil;

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
        CardView cardShelter = view.findViewById(R.id.cardShelter);
        CardView cardMap = view.findViewById(R.id.cardMaps);
        CardView cardDocument = view.findViewById(R.id.cardDocument);
        LinearLayout infoAsylumSeeker = view.findViewById(R.id.infoAsylumSeeker);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String typeUser = sharedPreferences.getString("typeUser", null);
        NavigationUtil.showBackButton(this);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (typeUser != null) {
            if (typeUser.equals("asylum_seeker")) {
                infoAsylumSeeker.setVisibility(View.VISIBLE);
            } else if (typeUser.equals("doctor")) {
                infoAsylumSeeker.setVisibility(View.GONE);
            }
        }

        cardShelter.setOnClickListener(view1 -> {
            Fragment fragment = new RefugeeShelterFragment();
            setFragmentAnimation(fragment);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        cardMap.setOnClickListener(view1 -> {
            Fragment fragment = new MapsFragment();
            setFragmentAnimation(fragment);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        cardDocument.setOnClickListener(view1 -> {
            exoPlayer = videoAdapter.getExoPlayerAtPosition(previousPosition);
            if (exoPlayer != null) {
                exoPlayer.pause();
            }
            DocumentAndContactDialog.showAsylumInfoDialog(requireContext());
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] titleList = {
                getString(R.string.nutritional_tips),
                getString(R.string.prevention_and_lifestyle),
                getString(R.string.purchase_of_drugs)
        };


        String videoTable = null;
        if (typeUser != null) {
            if (typeUser.equals("asylum_seeker")) {
                videoTable = "videos_asylum_seeker";
            } else if (typeUser.equals("doctor")) {
                videoTable = "videos_doctor";
            }
        }

        if (videoTable != null) {
            db.collection(videoTable).orderBy("number").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<VideoModel> newVideos = new ArrayList<>();
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String url = document.getString("url");
                        if (url != null) {
                            if (i == 0) {
                                videoList.clear();
                            }
                            videoList.add(new VideoModel(titleList[i], url));
                            progressBar.setVisibility(View.GONE);
                            videoAdapter.notifyItemInserted(i);
                            i++;
                        }
                    }
                }
            });
        }


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
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Informazioni");
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