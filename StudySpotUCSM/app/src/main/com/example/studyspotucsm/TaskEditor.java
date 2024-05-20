package com.example.studyspotucsm;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TaskEditor extends AppCompatActivity {
    private AppTask t;
    private EditText taskInput, professorName, Ubicacion;
    private TextView from, to;
    private Spinner color, spinnerBuilding, spinnerRoom, spinnerTipoClase;
    private Button submit;
    private TextView delete;
    private Database database;

    // Mapa que asocia cada sala con su ubicación
    private Map<String, String> roomLocationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        // Obtener el usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            database = new Database(user.getUid()); // Inicializa Database con el UID del usuario
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_LONG).show();
            finish(); // Cerrar la actividad si no hay usuario
            return;
        }

        // Inicialización de los controles UI
        taskInput = findViewById(R.id.task);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);
        color = findViewById(R.id.color);
        spinnerBuilding = findViewById(R.id.spinnerBuilding);
        spinnerRoom = findViewById(R.id.spinnerRoom);
        professorName = findViewById(R.id.professorName);
        submit = findViewById(R.id.submit);
        delete = findViewById(R.id.delete);
        Ubicacion = findViewById(R.id.Ubicacion);
        spinnerTipoClase = findViewById(R.id.spinnerTipoClase);

        String date = getIntent().getStringExtra("Date");

        // Configuración del spinner para los colores
        String[] colors = {"Rose", "Blue", "Green", "Red", "Yellow", "Orange", "Purple"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        color.setAdapter(adapter);

        ArrayAdapter<CharSequence> tipoClaseAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipo_clase, android.R.layout.simple_spinner_item);
        tipoClaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoClase.setAdapter(tipoClaseAdapter);

        ArrayAdapter<CharSequence> adapterBuilding = ArrayAdapter.createFromResource(this,
                R.array.buildings_array, android.R.layout.simple_spinner_item);
        adapterBuilding.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(adapterBuilding);

        spinnerBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomsBasedOnBuilding(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRoom = parent.getItemAtPosition(position).toString();
                String location = roomLocationMap.get(selectedRoom);
                if (location != null) {
                    // Muestra la ubicación asociada a la sala seleccionada
                    Ubicacion.setText(location);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (getIntent().hasExtra("ID")) {
            t = new AppTask();
            t.setId(getIntent().getIntExtra("ID", 0));
            t.setTask(getIntent().getStringExtra("Task"));
            t.setFrom(getIntent().getStringExtra("From"));
            t.setTo(getIntent().getStringExtra("To"));
            t.setColor(getIntent().getStringExtra("Color"));
            t.setProfessor(getIntent().getStringExtra("ProfessorName"));
            t.setBuilding(getIntent().getStringExtra("Building"));
            t.setRoom(getIntent().getStringExtra("Room"));
            t.setUbicacion(getIntent().getStringExtra("Ubicacion"));
            t.setTipoClase(getIntent().getStringExtra("TipoClase"));

            taskInput.setText(t.getTask());
            from.setText(t.getFromToString());
            to.setText(t.getToToString());
            color.setSelection(adapter.getPosition(t.getColor()));
            spinnerBuilding.setSelection(adapterBuilding.getPosition(t.getBuilding()));
            professorName.setText(t.getProfessor());
            Ubicacion.setText(t.getUbicacion());
        } else {
            t = new AppTask(); // Nueva instancia si es una nueva tarea
        }

        setupTimePicker(); // Configurar los selectores de tiempo

        submit.setOnClickListener(v -> handleTaskSubmission(date, adapter));
        delete.setOnClickListener(v -> handleTaskDeletion(date));
    }

    private void updateRoomsBasedOnBuilding(int buildingIndex) {
        int arrayId;
        switch (buildingIndex) {
            case 0: // Pabellón A
                arrayId = R.array.pabellon_a_rooms;
                break;
            case 1: // Pabellón B
                arrayId = R.array.pabellon_b_rooms;
                break;
            case 2: // Pabellón C
                arrayId = R.array.pabellon_c_rooms;
                break;
            case 3: // Pabellón D
                arrayId = R.array.pabellon_d_rooms;
                break;
            case 4: // Pabellón E
                arrayId = R.array.pabellon_e_rooms;
                break;
            case 5: // Pabellón CH
                arrayId = R.array.pabellon_ch_rooms;
                break;
            case 6: // Pabellón O
                arrayId = R.array.pabellon_o_rooms;
                break;
            case 7: // Pabellón G
                arrayId = R.array.pabellon_g_rooms;
                break;
            case 8: // Pabellón H
                arrayId = R.array.pabellon_h_rooms;
                break;
            case 9: // Pabellón I
                arrayId = R.array.pabellon_i_rooms;
                break;
            case 10: // Pabellón F
                arrayId = R.array.pabellon_f_rooms;
                break;
            case 11: // Pabellón L
                arrayId = R.array.pabellon_l_rooms;
                break;
            case 12: // Pabellón R
                arrayId = R.array.pabellon_r_rooms;
                break;
            case 13: // Pabellón S
                arrayId = R.array.pabellon_s_rooms;
                break;
            default:
                arrayId = R.array.default_rooms;
                break;
        }
        ArrayAdapter<CharSequence> adapterRoom = ArrayAdapter.createFromResource(this,
                arrayId, android.R.layout.simple_spinner_item);
        adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(adapterRoom);

        // Actualizar el mapa de ubicaciones de las salas
        String[] rooms = getResources().getStringArray(arrayId);
        roomLocationMap.clear(); // Limpiamos el mapa antes de actualizarlo

        if (buildingIndex == 0) { // Si se selecciona el Pabellón A
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "102":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "103":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "104":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "105":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "106":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "107":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "108":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "201":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "202":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "203":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "204":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "205":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "206":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "207":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "208":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "301":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "302":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "303":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "304":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "305":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "306":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "307":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "308":
                        roomLocationMap.put(room, "-16.405755, -71.549604");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón A - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 1) {
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "2015 Stierlin Ct, Mountain View, CA 94043, Estados Unidos");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón B - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }// Si se selecciona el Pabellón B
        } else if (buildingIndex == 2) { // Si se selecciona el Pabellón C
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 C");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón C - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 3) { // Si se selecciona el Pabellón D
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 D");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón D - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 4) { // Si se selecciona el Pabellón E
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 E");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón E - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 5) { // Si se selecciona el Pabellón CH
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 CH");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón CH - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 6) { // Si se selecciona el Pabellón O
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 O");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón O - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 7) { // Si se selecciona el Pabellón G
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 G");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón G - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 8) { // Si se selecciona el Pabellón H
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 H");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón H - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 9) { // Si se selecciona el Pabellón I
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 I");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón I - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 10) { // Si se selecciona el Pabellón F
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 F");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón F - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 11) { // Si se selecciona el Pabellón L
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "102":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "103":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "104":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "105":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "106":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "107":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "108":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "201":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "202":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "203":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "204":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "205":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "206":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "207":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "208":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "301":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "302":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "303":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "304":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "305":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "306":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "307":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "308":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "401":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "402":
                        roomLocationMap.put(room, "-16.405548, -71.547966");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón L - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 12) { // Si se selecciona el Pabellón R
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 R");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón R - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else if (buildingIndex == 13) { // Si se selecciona el Pabellón S
            for (String room : rooms) {
                switch (room) {
                    case "101":
                        roomLocationMap.put(room, "-16.405753, -71.549554 S");
                        break;
                    case "102":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 102");
                        break;
                    case "103":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 103");
                        break;
                    case "104":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 104");
                        break;
                    case "105":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 105");
                        break;
                    case "106":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 106");
                        break;
                    case "107":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 107");
                        break;
                    case "108":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 108");
                        break;
                    case "201":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 201");
                        break;
                    case "202":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 202");
                        break;
                    case "203":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 203");
                        break;
                    case "204":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 204");
                        break;
                    case "205":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 205");
                        break;
                    case "206":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 206");
                        break;
                    case "207":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 207");
                        break;
                    case "208":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 208");
                        break;
                    case "301":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 301");
                        break;
                    case "302":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 302");
                        break;
                    case "303":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 303");
                        break;
                    case "304":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 304");
                        break;
                    case "305":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 305");
                        break;
                    case "306":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 306");
                        break;
                    case "307":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 307");
                        break;
                    case "308":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 308");
                        break;
                    case "401":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 401");
                        break;
                    case "402":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 402");
                        break;
                    case "403":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 403");
                        break;
                    case "404":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 404");
                        break;
                    case "405":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 405");
                        break;
                    case "406":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 406");
                        break;
                    case "407":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 407");
                        break;
                    case "408":
                        roomLocationMap.put(room, "Ubicación del Pabellón S - Sala 408");
                        break;
                    default:
                        roomLocationMap.put(room, "Ubicación desconocida");
                        break;
                }
            }
        } else {

        }
    }
    private void handleTaskSubmission(String date, ArrayAdapter<String> adapter) {
        if (taskInput.getText().toString().isEmpty()) {
            taskInput.setError("Task cannot be empty");
            return;
        }

        // Actualiza la tarea con los nuevos valores ingresados
        updateTaskDetails();

        // Guarda la tarea inicial y programa las tareas repetitivas
        if (t.getId() != 0) {
            database.updateTask(t, date);
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            database.addTask(t, date);  // Guarda la tarea inicial
            scheduleRecurringTasks(t, date);  // Programa las repeticiones semanales
            Toast.makeText(this, "Task added and scheduled successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void updateTaskDetails() {
        t.setTask(taskInput.getText().toString());
        t.setColor(color.getSelectedItem().toString());
        t.setProfessor(professorName.getText().toString());
        t.setBuilding(spinnerBuilding.getSelectedItem().toString());
        t.setRoom(spinnerRoom.getSelectedItem().toString());
        t.setUbicacion(Ubicacion.getText().toString());
        t.setTipoClase(spinnerTipoClase.getSelectedItem().toString());
        // No necesitas configurar 'from' y 'to' aquí ya que se configuran en el TimePicker
    }

    private void scheduleRecurringTasks(AppTask task, String initialDate) {
        LocalDate startDate = LocalDate.parse(initialDate);
        for (int i = 1; i <= 52; i++) {  // Repite por un año
            LocalDate nextDate = startDate.plusWeeks(i);
            AppTask newTask = new AppTask(task);  // Usando el constructor de copia
            database.addTask(newTask, nextDate.toString());
        }
    }
    private void handleTaskDeletion(String date) {
        if (t.getId() != 0) {
            database.deleteTask(String.valueOf(t.getId()), date);
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private void setupTimePicker() {
        TimePickerDialog.OnTimeSetListener fromTimeListener = (view, hourOfDay, minute) -> {
            String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
            from.setText(formattedTime);
            t.setFrom(formattedTime);
        };

        TimePickerDialog.OnTimeSetListener toTimeListener = (view, hourOfDay, minute) -> {
            String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
            to.setText(formattedTime);
            t.setTo(formattedTime);
        };

        from.setOnClickListener(v -> {
            LocalTime currentTime = LocalTime.now();
            new TimePickerDialog(this, fromTimeListener, currentTime.getHour(), currentTime.getMinute(), true).show();
        });

        to.setOnClickListener(v -> {
            LocalTime currentTime = LocalTime.now();
            new TimePickerDialog(this, toTimeListener, currentTime.getHour(), currentTime.getMinute(), true).show();
        });
    }
}