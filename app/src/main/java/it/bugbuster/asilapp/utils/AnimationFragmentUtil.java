package it.bugbuster.asilapp.utils;

import android.transition.Fade;

import androidx.fragment.app.Fragment;

public class AnimationFragmentUtil {
    public static void setFragmentAnimation(Fragment fragment) {
        fragment.setEnterTransition(new Fade(Fade.IN));
        fragment.setExitTransition(new Fade(Fade.OUT));
    }
}
