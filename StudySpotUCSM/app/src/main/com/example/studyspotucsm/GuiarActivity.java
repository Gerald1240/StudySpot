package com.example.studyspotucsm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class GuiarActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView ubicacionTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private MaterialButton homeButton;
    TextView emailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guiar);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());
            return insets;
        });
        emailTextView = findViewById(R.id.emailTextView);

        ubicacionTextView = findViewById(R.id.Ubicacion);
        homeButton = findViewById(R.id.homeButton);// Asegúrate de que el ID del TextView es correcto

        Intent intent = getIntent();
        String destination = intent.getStringExtra("Ubicacion");
        ubicacionTextView.setText(destination);  // Establecer la ubicación en el TextView

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        MaterialButton backButton = findViewById(R.id.backButton1);
        backButton.setOnClickListener(v -> finish());

        MaterialButton cameraButton = findViewById(R.id.camaraButtom);
        cameraButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(GuiarActivity.this, CameraActivity.class);
            String destinationCoordinates = ubicacionTextView.getText().toString(); // asegúrate que esté en el formato "lat,lng"
            cameraIntent.putExtra("destination", destinationCoordinates);
            startActivity(cameraIntent);
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        emailTextView.setText(user.getEmail());

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuiarActivity.this, UserActivity.class);
                startActivity(intent);
                finish(); // Cierra CameraActivity
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void setupMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    drawRoute(currentLocation); // Llamar a drawRoute con la ubicación actual

                    // Mover la cámara al lugar actual y ajustar el nivel de zoom
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Ajusta el segundo argumento para el nivel de zoom deseado
                } else {
                    ubicacionTextView.setText("No se pudo obtener la ubicación.");
                }
            });
        }
    }

    private void drawRoute(LatLng origin) {
        if (origin == null) {
            Toast.makeText(this, "Error: Origin is null.", Toast.LENGTH_LONG).show();
            return;
        }

        String destinationAddress = ubicacionTextView.getText().toString();  // Utilizar la dirección de destino del TextView

        String apiKey = "AIzaSyCj1v88FVMvN06LkuugQ6fUQZpiD5HhZ2w";
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                origin.latitude + "," + origin.longitude +
                "&destination=" + Uri.encode(destinationAddress) +
                "&key=" + apiKey;

        StringRequest directionsRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(polyline)).color(Color.parseColor("#109c54")));
                        } else {
                            Toast.makeText(this, "No se encontraron rutas.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al analizar la respuesta.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(GuiarActivity.this, "Error en la solicitud: " + error.getMessage(), Toast.LENGTH_LONG).show());

        Volley.newRequestQueue(this).add(directionsRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
