package it.bugbuster.asilapp.access;

import static it.bugbuster.asilapp.utils.AnimationFragmentUtil.setFragmentAnimation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.bugbuster.asilapp.dialog.MyBottomSheetDialogFragment;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.dialog.ReviewAppDialog;
import it.bugbuster.asilapp.tab.TabsFragment;
import it.bugbuster.asilapp.database.DiseasesDatabase;
import it.bugbuster.asilapp.database.ExpensesDatabase;
import it.bugbuster.asilapp.database.MeasurementsDatabase;
import it.bugbuster.asilapp.expenses.ExpenseListFragment;
import it.bugbuster.asilapp.information.InformationFragment;
import it.bugbuster.asilapp.profile.ProfileFragment;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;


public class HomeAsylumSeeker extends AppCompatActivity {
    private FloatingActionButton fab;
    private ExpensesDatabase expensesDatabase;
    private DiseasesDatabase diseasesDatabase;
    private MeasurementsDatabase measurementsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_asylum_seeker);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        fab = findViewById(R.id.floating_action_button);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        diseasesDatabase = new DiseasesDatabase(this);
        expensesDatabase = new ExpensesDatabase(this);
        measurementsDatabase = new MeasurementsDatabase(this);
        initializeLocalDataToFirestore();
        initializeFirestoreToLocalData();

        SharedPreferences sharedPreferences = this.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String userId = AuthUtils.getCurrentUserId();
        int launchCount = sharedPreferences.getInt("launch_count_" + userId, 0);
        boolean dontAskAgain = sharedPreferences.getBoolean("dont_ask_again_" + userId, false);

        if (savedInstanceState == null) {
            NavigationUtil.showHomeButton(this);
            Fragment fragment = new TabsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        if (launchCount >= 1 && !dontAskAgain) {
            ReviewAppDialog reviewAppDialog = new ReviewAppDialog(this);
            reviewAppDialog.showDialog();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("launch_count_" + userId, launchCount + 1);
        editor.apply();

        fab.setOnClickListener(view -> {
            MyBottomSheetDialogFragment bottomSheet = new MyBottomSheetDialogFragment();
            bottomSheet.show(getSupportFragmentManager(), "MyBottomSheetDialog");
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new TabsFragment();
            } else if (item.getItemId() == R.id.nav_info){
                selectedFragment = new InformationFragment();
            } else if (item.getItemId() == R.id.nav_list){
                selectedFragment = new ExpenseListFragment();
            } else if (item.getItemId() == R.id.nav_profile){
                selectedFragment = new ProfileFragment();
            }


            if (item.getItemId() != R.id.nav_home) {
                NavigationUtil.showBackButton(this);
            }

            if (selectedFragment != null) {
                setFragmentAnimation(selectedFragment);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack("home")
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

                    if ("home".equals(backStackName)) {
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                        if (!(currentFragment instanceof TabsFragment)) {
                            bottomNav.setSelectedItemId(R.id.nav_home);
                            Fragment fragment = new TabsFragment();
                            setFragmentAnimation(fragment);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
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

    private void initializeLocalDataToFirestore() {
        diseasesDatabase.syncLocalDataToFirestore(this);
        expensesDatabase.syncLocalDataToFirestore(this);
        measurementsDatabase.syncLocalDataToFirestore(this);
    }

    private void initializeFirestoreToLocalData() {
        diseasesDatabase.syncFirestoreToLocal(this);
        expensesDatabase.syncFirestoreToLocal(this);
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