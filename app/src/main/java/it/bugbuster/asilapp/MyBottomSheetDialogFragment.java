package it.bugbuster.asilapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.journeyapps.barcodescanner.ScanOptions;

import it.bugbuster.asilapp.access.Login;
import it.bugbuster.asilapp.expenses.AddExpenseFragment;
import it.bugbuster.asilapp.measurements.TakeMeasurementsFragment;

public class MyBottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private static final int BLUETOOTH_PERMISSION_REQUEST = 200;
/*
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);



        return view;
    }

 */

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                requireContext(), R.style.ModalBottomSheetDialog
        );
        bottomSheetDialog.setContentView(R.layout.fragment_bottom_sheet);

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        // Close button functionality
        Button btnAddExpanse = bottomSheetDialog.findViewById(R.id.add_expanse);
        Button btnTemperature = bottomSheetDialog.findViewById(R.id.temperature);
        Button btnHeartBeat = bottomSheetDialog.findViewById(R.id.hearthbeat);

        if (btnAddExpanse != null) {
            btnAddExpanse.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_list);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddExpenseFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (btnTemperature != null) {
            btnTemperature.setOnClickListener(v -> {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(requireContext(), getString(R.string.bluetooth_not_found), Toast.LENGTH_SHORT).show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.BLUETOOTH_CONNECT) ==
                                PackageManager.PERMISSION_GRANTED) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            bluetoothLauncher.launch(enableBtIntent);
                        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                            // In an educational UI, explain to the user why your app requires this
                            // permission for a specific feature to behave as expected, and what
                            // features are disabled if it's declined. In this UI, include a
                            // "cancel" or "no thanks" button that lets the user continue
                            // using your app without granting the permission.
                        } else {
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                            } else {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                bluetoothLauncher.launch(enableBtIntent);
                            }
                        }

                    }
                }
                /*
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.TEMPERATURE))
                        .addToBackStack(null)
                        .commit();

                 */
            });
        }

        if (btnHeartBeat != null) {
            btnHeartBeat.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.HEARTBEAT))
                        .addToBackStack(null)
                        .commit();
            });
        }



        return bottomSheetDialog;
    }
    private final ActivityResultLauncher<Intent> bluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                    result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {

        }
    });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    bluetoothLauncher.launch(enableBtIntent);
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });


    @Override
    public int getTheme() {
        return R.style.ModalBottomSheetDialog;
    }
}