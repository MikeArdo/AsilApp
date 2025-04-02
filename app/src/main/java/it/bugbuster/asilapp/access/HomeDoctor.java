package it.bugbuster.asilapp.access;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.bugbuster.asilapp.ReviewAppDialog;
import it.bugbuster.asilapp.information.InformationFragment;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.diseases.AsylumSeekersListFragment;
import it.bugbuster.asilapp.profile.ProfileFragment;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;


public class HomeDoctor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_doctor);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        SharedPreferences sharedPreferences = this.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String userId = AuthUtils.getCurrentUserId();
        int launchCount = sharedPreferences.getInt("launch_count_" + userId, 0);
        boolean dontAskAgain = sharedPreferences.getBoolean("dont_ask_again_" + userId, false);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            NavigationUtil.showHomeButton(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AsylumSeekersListFragment())
                    .commit();
        }

        if (launchCount >= 1 && !dontAskAgain) {
            ReviewAppDialog reviewAppDialog = new ReviewAppDialog(this);
            reviewAppDialog.showDialog();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("launch_count_" + userId, launchCount + 1);
        editor.apply();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new AsylumSeekersListFragment();
            } else if (item.getItemId() == R.id.nav_info){
                selectedFragment = new InformationFragment();
            } else if (item.getItemId() == R.id.nav_profile){
                selectedFragment = new ProfileFragment();
            }


            if (item.getItemId() != R.id.nav_home) {
                NavigationUtil.showBackButton(this);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack("home_doctor")
                        .commit();
            }
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

                if (backStackCount > 0) {
                    String backStackName = getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();

                    if ("home_doctor".equals(backStackName)) {
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                        if (!(currentFragment instanceof AsylumSeekersListFragment)) {
                            bottomNav.setSelectedItemId(R.id.nav_home);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new AsylumSeekersListFragment())
                                    .commit();
                        } else {
                            finish();
                        }
                    } else {
                        getSupportFragmentManager().popBackStack();
                    }
                } else {
                    finish();
                }
            }
        });


    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}