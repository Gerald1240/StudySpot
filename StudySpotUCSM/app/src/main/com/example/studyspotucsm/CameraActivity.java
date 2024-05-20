package com.example.studyspotucsm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;

public class CameraActivity extends AppCompatActivity {

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private TextView directionDistanceView;
    private LocationCallback locationCallback;
    private ImageView directionArrowView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng destinationLatLng;
    private MaterialButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        directionDistanceView = findViewById(R.id.directionDistanceView);
        directionArrowView = findViewById(R.id.directionArrow);  // Asegúrate de que este ID es correcto
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        backButton = findViewById(R.id.backButton);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        updateDirectionAndDistance(location);
                    }
                }
            }
        };

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, UserActivity.class);
                startActivity(intent);
                finish(); // Cierra CameraActivity
            }
        });

        Intent intent = getIntent();
        String destinationCoordinates = intent.getStringExtra("destination");
        if (destinationCoordinates != null && !destinationCoordinates.isEmpty()) {
            String[] parts = destinationCoordinates.split(",");
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            destinationLatLng = new LatLng(lat, lng);
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);  // Actualizar ubicación cada segundo
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        updateDirectionAndDistance(location);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateDirectionAndDistance(Location currentLocation) {
        if (currentLocation != null && destinationLatLng != null) {
            Location destinationLocation = new Location("");
            destinationLocation.setLatitude(destinationLatLng.latitude);
            destinationLocation.setLongitude(destinationLatLng.longitude);

            float distance = currentLocation.distanceTo(destinationLocation);
            float bearingTo = currentLocation.bearingTo(destinationLocation);
            String distanceFormatted = String.format("%.2f", distance);
            directionDistanceView.setText("Dirección: " + getDirectionFromDegrees(bearingTo) + "\nDistancia: " + distanceFormatted + " metros");

            directionArrowView.setRotation(bearingTo - currentLocation.getBearing());
        }
    }

    private String getDirectionFromDegrees(float degrees) {
        if (degrees > 45 && degrees <= 135) {
            return "ESTE";
        } else if (degrees > 135 && degrees <= 225) {
            return "SUR";
        } else if (degrees > 225 && degrees <= 315) {
            return "OESTE";
        } else {
            return "NORTE";
        }
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            // Manejar cambios de tamaño si es necesario
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return true;  // Destruir el SurfaceTexture cuando el TextureView se destruya
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // Actualizar la vista según sea necesario cuando el TextureView se actualice
        }
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];  // Asumir que la primera cámara es la cámara trasera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 102);
                return;
            }
            manager.openCamera(cameraId, cameraStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "No se puede acceder a la cámara.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Toast.makeText(CameraActivity.this, "Error de cámara: " + error, Toast.LENGTH_SHORT).show();
        }
    };

    private void startPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(CameraActivity.this, "No se pudo configurar la cámara.", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
