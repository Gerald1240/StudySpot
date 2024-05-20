package com.example.studyspotucsm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HorarioActivity extends AppCompatActivity {
    private MaterialButton homeButton;
    TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horario);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets;
        });

        TextView courseNameTextView = findViewById(R.id.courseName);
        TextView classroomTextView = findViewById(R.id.classroom);
        TextView professorTextView = findViewById(R.id.professor);
        TextView hoursTextView = findViewById(R.id.hours);
        TextView buildingTextView = findViewById(R.id.building);
        TextView ubicacionTextView = findViewById(R.id.ubicacion);
        emailTextView = findViewById(R.id.emailTextView);
        homeButton = findViewById(R.id.homeButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String courseName = extras.getString("Task", "Información no disponible");
            String tipoClase = extras.getString("TipoClase", "");
            String classroom = extras.getString("Room", "Información no disponible");
            String professorName = extras.getString("ProfessorName", "Información no disponible");
            String fromTime = extras.getString("From", "Información no disponible");
            String toTime = extras.getString("To", "Información no disponible");
            String hours = fromTime + " - " + toTime;
            String building = extras.getString("Building", "Información no disponible");
            String ubicacion = extras.getString("Ubicacion", "Información no disponible");

            courseName = courseName + " - " + tipoClase;

            courseNameTextView.setText(courseName);
            classroomTextView.setText("Aula: " + classroom);
            professorTextView.setText("Profesor: " + professorName);
            hoursTextView.setText("Horas: " + hours);
            buildingTextView.setText("Edificio: " + building);
            ubicacionTextView.setText("Ubicación: " + ubicacion);
        } else {
            Log.d("HorarioActivity", "No extras found!");
            // Default values if no extras found
            courseNameTextView.setText("Curso: Información no disponible");
            classroomTextView.setText("Aula: Información no disponible");
            professorTextView.setText("Profesor: Información no disponible");
            hoursTextView.setText("Horas: Información no disponible");
            buildingTextView.setText("Edificio: Información no disponible");
            ubicacionTextView.setText("Ubicación: Información no disponible");
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        emailTextView.setText(user.getEmail());

        MaterialButton guiarButton = findViewById(R.id.guiar);
        guiarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HorarioActivity.this, GuiarActivity.class);
                if (extras != null) {
                    String ubicacion = extras.getString("Ubicacion", "Información no disponible");
                    intent.putExtra("Ubicacion", ubicacion); // Pasar solo el valor de la ubicación
                } else {
                    intent.putExtra("Ubicacion", "Información no disponible"); // Valor por defecto si no hay extras
                }
                startActivity(intent);
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HorarioActivity.this, UserActivity.class);
                startActivity(intent);
                finish(); // Cierra CameraActivity
            }
        });


        MaterialButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Closes the activity and returns to the previous one
            }
        });
    }
}
