package it.bugbuster.asilapp.measurements;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.database.MeasurementsDatabase;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class TakeMeasurementsFragment extends Fragment implements SensorEventListener {
    private MeasurementsDatabase dbMeasurements;
    private static final String TYPE_MEASUREMENT = "type_measurement";
    private CircularProgressIndicator progressIndicator;
    private Button startButton, shareButton;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView valMeasurement;
    private boolean isMeasuring;
    private Measurements typeMeasurement;
    private TextView textMeasurement;

    private Handler handler = new Handler();
    private Random random = new Random();

    public TakeMeasurementsFragment() {
    }

    public static TakeMeasurementsFragment newInstance(Measurements typeMeasurement) {
        TakeMeasurementsFragment fragment = new TakeMeasurementsFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_MEASUREMENT, typeMeasurement.name());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            typeMeasurement = Measurements.valueOf(getArguments().getString(TYPE_MEASUREMENT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_measurements, container, false);

        dbMeasurements = new MeasurementsDatabase(getContext());
        progressIndicator = view.findViewById(R.id.circularProgressIndicator);
        textMeasurement = view.findViewById(R.id.textMeasurement);
        startButton = view.findViewById(R.id.startButton);
        shareButton = view.findViewById(R.id.shareButton);
        valMeasurement = view.findViewById(R.id.valMeasurement);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        isMeasuring = false;

        NavigationUtil.showBackButton(this);

        if (sensorManager != null && typeMeasurement == Measurements.TEMPERATURE) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        switch (typeMeasurement) {
            case TEMPERATURE:
                textMeasurement.setText(getString(R.string.temperature_measurement));
                break;
            case HEARTBEAT:
                textMeasurement.setText(getString(R.string.heartbeat_measurement));
                break;
            case WEIGHT:
                textMeasurement.setText(getString(R.string.weight_measurement));
                break;
            case GLYCEMIA:
                textMeasurement.setText(getString(R.string.blood_glucose_measurement));
                break;
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgress();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareResults();
            }
        });

        return view;
    }

    private void startProgress() {
        progressIndicator.setProgress(0);
        isMeasuring = true;
        startButton.setEnabled(false);
        shareButton.setVisibility(View.GONE);
        switch (typeMeasurement) {
            case TEMPERATURE:
                setTemperature(0);
                break;
            case HEARTBEAT:
                setHeartBeat();
                break;
            case WEIGHT:
                setWeight();
                break;
            case GLYCEMIA:
                setGlycemia();
                break;
        }
        new CountDownTimer(5000, 50) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((5000 - millisUntilFinished) * 100 / 5000);
                progressIndicator.setProgress(progress);
            }

            public void onFinish() {
                progressIndicator.setProgress(100);
                handler.removeCallbacksAndMessages(null);
                isMeasuring = false;
                startButton.setEnabled(true);
                shareButton.setVisibility(View.VISIBLE);
                saveMeasurement();
            }
        }.start();
    }

    private void setTemperature(float distance) {
        if (distance >= 0 && distance < 2) {
            valMeasurement.setText("36°C");
        } else if (distance >= 2 && distance < 4) {
            valMeasurement.setText("37°C");
        } else if (distance >= 4 && distance < 6) {
            valMeasurement.setText("38°C");
        } else if (distance >= 6 && distance < 8) {
            valMeasurement.setText("39°C");
        } else if (distance >= 8 && distance < 10) {
            valMeasurement.setText("40°C");
        }
    }

    private int getRandomNumber(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private void setHeartBeat() {
        int heartRate = getRandomNumber(60, 120);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int preciseHeartRate = getRandomNumber(heartRate-5, heartRate+5);
                valMeasurement.setText(preciseHeartRate + " BPM");

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }



    private void setWeight() {
        int weight = getRandomNumber(50, 120);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int preciseWeight = getRandomNumber(weight-5, weight+5);
                valMeasurement.setText(preciseWeight + " kg");

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void setGlycemia() {
        int glycemia = getRandomNumber(70, 140);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int preciseGlycemia = getRandomNumber(glycemia-5, glycemia+5);
                valMeasurement.setText(preciseGlycemia + " mg/dL");

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void shareResults() {
        String message = getString(R.string.my_measurement_data);
        switch (typeMeasurement) {
            case TEMPERATURE:
                message += getString(R.string.temperature);
                break;
            case HEARTBEAT:
                message += getString(R.string.heartbeat);
                break;
            case WEIGHT:
                message += getString(R.string.weight);
                break;
            case GLYCEMIA:
                message += getString(R.string.blood_glucose);
                break;
        }

        message += valMeasurement.getText() + getString(R.string.generated_by_asilapp);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_data));
        startActivity(shareIntent);
    }

    private void saveMeasurement() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        String typeMeasurement = String.valueOf(this.typeMeasurement);
        String valueMeasurement = valMeasurement.getText().toString();


        boolean inserted = dbMeasurements.addMeasurement(getContext(), typeMeasurement, formattedDateTime, valueMeasurement);

        if (inserted) {
            Toast.makeText(getContext(), getString(R.string.measurement_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.measurement_not_saved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float distance;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY && isMeasuring) {
            distance = sensorEvent.values[0];
            setTemperature(distance);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Effettua misurazione");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (proximitySensor != null) {
            sensorManager.unregisterListener(this);
        }
    }
}