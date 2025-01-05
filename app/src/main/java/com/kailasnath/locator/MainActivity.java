package com.kailasnath.locator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Button start_btn;
    Button record_btn;
    EditText et_ip;

    private boolean clicked = true;
    private boolean record = false;
    private String url;

    private boolean location_permission_code = false;
    private boolean internet_permission_code = false;
    ActivityResultLauncher<String[]> permissionLauncher;

    // Location elements ==================================
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    //LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_btn = findViewById(R.id.button_start);
        record_btn = findViewById(R.id.button_record);
        et_ip = findViewById(R.id.editText_ip);


        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> o) {
                        if (o.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                            location_permission_code = Boolean.TRUE.equals(o.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        }
                        if (o.get(Manifest.permission.INTERNET) != null) {
                            internet_permission_code = Boolean.TRUE.equals(o.get(Manifest.permission.INTERNET));
                        }
                    }
                });

        requestPermission();

        // initializing location elements =========================
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);


                Location location = locationResult.getLastLocation();

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                assert location != null;

                setLatLon(latitude, longitude);

                BusLocationAndRecordStatus busLocationAndRecordStatus = new BusLocationAndRecordStatus(1, latitude, longitude, record);

                Gson gson = new Gson();
                String json = gson.toJson(busLocationAndRecordStatus);
                sendHttpReq(json);
            }
        };

        locationRequest = new LocationRequest();
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10 * 1000);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //===========================================================

        getLocationUpdates();
        start_btn.setOnClickListener(view -> startTracking());
        record_btn.setOnClickListener(view -> switchRecord());
    }

    private void switchRecord() {
        record = !record;

        if (record)
            record_btn.setText("Stop record");
        else
            record_btn.setText("Start record");
    }

    private void sendHttpReq(String json) {
        if (!url.equals("0")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Sending location failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private String getUrl() {
        String ip = et_ip.getText().toString();
        String url_domain = "http://";
        String end_point = ":8080/update";

        if (!ip.isEmpty())
            return url_domain + ip + end_point;

        Toast.makeText(this, "Specify IP address", Toast.LENGTH_SHORT).show();
        return "0";
    }

    private void getLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, null);

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                //updating ui
                                setLatLon(location.getLatitude(), location.getLongitude());

                            } else {
                                setLatLon(0.0, 0.0);
                                Toast.makeText(MainActivity.this, "Location is turned off", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void startTracking() {
        if (clicked) {
            start_btn.setText("Stop tracking");

            try {
                //tracking location
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                url = getUrl();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            start_btn.setText("Start tracking");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            getLocationUpdates();
        }

        clicked = !clicked;
    }

    private void setLatLon(double latitude, double longitude) {
        TextView lat_tv = findViewById(R.id.latitude);
        TextView lom_tv = findViewById(R.id.longitude);

        lat_tv.setText(Double.toString(latitude));
        lom_tv.setText(Double.toString(longitude));
    }

    private void requestPermission() {
        location_permission_code = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        internet_permission_code = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED;

        List<String> unauthorizedPermissions = new ArrayList<>();

        if (!location_permission_code) {
            unauthorizedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!internet_permission_code) {
            unauthorizedPermissions.add(Manifest.permission.INTERNET);
        }

        if (!unauthorizedPermissions.isEmpty()) {
            permissionLauncher.launch(unauthorizedPermissions.toArray(new String[0]));
        }
    }
}