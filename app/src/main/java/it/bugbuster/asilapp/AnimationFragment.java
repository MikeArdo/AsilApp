package it.bugbuster.asilapp;

import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class AnimationFragment {
    public static void setFragmentAnimation(Fragment fragment) {
        fragment.setEnterTransition(new Fade(Fade.IN));
        fragment.setExitTransition(new Fade(Fade.OUT));
    }
}
