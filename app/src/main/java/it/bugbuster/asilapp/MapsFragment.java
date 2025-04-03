package it.bugbuster.asilapp;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.google.protobuf.StringValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.bugbuster.asilapp.utils.LanguageUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesClient placesClient;
    String apiKey = BuildConfig.MAPS_API_KEY;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 14;
    private final LatLng defaultLocation = new LatLng(41.1097315, 16.8800639);
    private String cityRefugeeShelter;
    private Location lastKnownLocation;

    public MapsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationUtil.showBackButton(this);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Mappa");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationUtil.showBackButton(this);
        Places.initializeWithNewPlacesApiEnabled(requireContext(), apiKey);
        placesClient = Places.createClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        cityRefugeeShelter = sharedPreferences.getString("refugeeShelter", null);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng cityLatLng = getLocationFromCityName(cityRefugeeShelter);
        if (cityLatLng != null) {
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(cityLatLng, DEFAULT_ZOOM));
            fetchNearbyPlaces(cityLatLng.latitude, cityLatLng.longitude);
        } else {
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            fetchNearbyPlaces(defaultLocation.latitude, defaultLocation.longitude);
        }

        mMap.setOnMyLocationButtonClickListener(() -> {
            promptEnableGPS();
            return false;
        });

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();
    }

    private LatLng getLocationFromCityName(String cityName) {
        Geocoder geocoder = new Geocoder(requireContext());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation == null) {
                                promptEnableGPS();
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.location_permission_description)
                    .setNegativeButton(requireContext().getResources().getString(R.string.no_thanks), (dialog, which) -> {
                        Toast.makeText(requireContext(), R.string.location_permission_denied,
                                Toast.LENGTH_LONG).show();
                    })
                    .setPositiveButton(requireContext().getResources().getString(R.string.continue_text), (dialog, which) -> {
                        requestPermissionLauncher.launch(new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    });
            dialogBuilder.create();
            dialogBuilder.show();
        } else {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.location_permission_description)
                    .setPositiveButton(requireContext().getResources().getString(R.string.continue_text), (dialog, which) -> {
                        requestPermissionLauncher.launch(new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    });
            dialogBuilder.create();
            dialogBuilder.show();
        }
    }

    private void promptEnableGPS() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(requireActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                            resolvable.getResolution()).build();
                    requestGpsResult.launch(intentSenderRequest);
                }
            }
        });
    }

    private final ActivityResultLauncher<IntentSenderRequest> requestGpsResult =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateLocationUI();
                }
            });

    public void fetchNearbyPlaces(double latitude, double longitude) {
        // Define a list of fields to include in the response for each returned place.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PRIMARY_TYPE);

        LatLng center = new LatLng(latitude, longitude);
        CircularBounds circle = CircularBounds.newInstance(center, /* radius = */ 1000);

        // Define a list of types to include.
        final List<String> includedTypes = Arrays.asList("hospital", "city_hall", "post_office", "church", "school", "pharmacy");

        // Use the builder to create a SearchNearbyRequest object.
        final SearchNearbyRequest searchNearbyRequest =
                SearchNearbyRequest.builder(/* location restriction = */ circle, placeFields)
                        .setIncludedPrimaryTypes(includedTypes)
                        .setMaxResultCount(10)
                        .build();

        // Call placesClient.searchNearby() to perform the search.
        // Define a response handler to process the returned List of Place objects.
        placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener(response -> {
                    List<Place> places = response.getPlaces();
                    for (Place place : places) {
                        LatLng placeLatLng = place.getLatLng();
                        if (placeLatLng != null) {
                            // Add a marker for each place
                            mMap.addMarker(new MarkerOptions()
                                    .position(placeLatLng)
                                    .title(place.getName())
                                    .snippet(getTranslation(place.getPrimaryType())));
                        }
                    }
                });
    }

    private String getTranslation(String type) {
        Map<String, String[]> types = new HashMap<>();
        types.put("hospital", new String[]{"Hospital", "Ospedale"});
        types.put("city_hall", new String[]{"City hall", "Municipio"});
        types.put("post_office", new String[]{"Post office", "Ufficio postale"});
        types.put("church", new String[]{"Church", "Chiesa"});
        types.put("school", new String[]{"School", "Scuola"});
        types.put("pharmacy", new String[]{"Pharmacy", "Farmacia"});

        if (types.containsKey(type)) {
            if (LanguageUtils.getCurrentLanguage().equals("it")) {
                String[] value = types.get(type);
                return value != null ? value[1] : null;
            } else {
                String[] value = types.get(type);
                return value != null ? value[0] : null;
            }
        } else {
            return "";
        }
    }




    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = null;
                Boolean coarseLocationGranted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION,false);
                }

                if (fineLocationGranted != null && fineLocationGranted) {
                    locationPermissionGranted = true;
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    locationPermissionGranted = true;
                } else {
                    locationPermissionGranted = false;
                }
                updateLocationUI();
                if (locationPermissionGranted) {
                    getDeviceLocation();
                }
            });
}