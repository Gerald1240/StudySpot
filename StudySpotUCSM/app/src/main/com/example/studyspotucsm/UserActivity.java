package com.example.studyspotucsm;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    private LocalDate showedDate;
    private ArrayList<AppTask> tasks;
    private final DateTimeFormatter mainDate = DateTimeFormatter.ofPattern("EEEE dd/MM");

    TextView emailTextView, date;
    MaterialButton logoutButton, horarioButton;
    ImageView left, right, addButton;
    ListView listview;
    private Database database;

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        database = new Database(user.getUid());

        emailTextView = findViewById(R.id.emailTextView);
        logoutButton = findViewById(R.id.logoutButton);
        date = findViewById(R.id.date);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        listview = findViewById(R.id.listview);
        addButton = findViewById(R.id.addButton);

        tasks = new ArrayList<>();
        listAdapter = new ListAdapter(this);
        listview.setAdapter(listAdapter);

        showedDate = LocalDate.now();
        RefreshData();

        date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
                showedDate = LocalDate.of(year, month + 1, day);
                RefreshData();
            }, showedDate.getYear(), showedDate.getMonthValue() - 1, showedDate.getDayOfMonth());
            datePickerDialog.show();
        });

        emailTextView.setText(user.getEmail());

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserActivity.this, LoginActivity.class));
            finish();
        });

        left.setOnClickListener(v -> {
            showedDate = showedDate.minusDays(1);
            RefreshData();
        });

        right.setOnClickListener(v -> {
            showedDate = showedDate.plusDays(1);
            RefreshData();
        });

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, TaskEditor.class);
            intent.putExtra("Date", showedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RefreshData();
    }

    private void RefreshData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
        date.setText(showedDate.format(formatter).substring(0, 1).toUpperCase() + showedDate.format(formatter).substring(1));

        database.getAllTasks(showedDate.format(DateTimeFormatter.ISO_LOCAL_DATE), new Database.OnTaskReceivedListener() {
            @Override
            public void onTaskReceived(ArrayList<AppTask> ts) {
                tasks.clear();
                tasks.addAll(ts);
                Collections.sort(tasks);
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    public class ListAdapter extends BaseAdapter {
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public AppTask getItem(int i) {
            return tasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.task, viewGroup, false);

            TextView from = v.findViewById(R.id.from);
            TextView to = v.findViewById(R.id.to);
            TextView task = v.findViewById(R.id.task);
            TextView additionalDetails = v.findViewById(R.id.additionalDetails);

            AppTask t = getItem(i);

            from.setText(t.getFromToString());
            to.setText(t.getToToString());
            task.setText(t.getTask() + " - " + t.getTipoClase());
            additionalDetails.setText(t.getBuilding() + " - " + t.getRoom());

            Drawable background = task.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(t.getColor(context));
            }

            LinearLayout taskContainer = v.findViewById(R.id.task_container);
            taskContainer.setOnClickListener(v1 -> {
                Intent intent = new Intent(UserActivity.this, HorarioActivity.class);
                intent.putExtra("ID", t.getId());
                intent.putExtra("Task", t.getTask());
                intent.putExtra("From", t.getFromToString());
                intent.putExtra("To", t.getToToString());
                intent.putExtra("Color", t.getColor());
                intent.putExtra("Date", showedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                intent.putExtra("Building", t.getBuilding());
                intent.putExtra("Room", t.getRoom());
                intent.putExtra("Ubicacion", t.getUbicacion());
                intent.putExtra("TipoClase", t.getTipoClase());
                intent.putExtra("ProfessorName", t.getProfessor());
                context.startActivity(intent);
            });

            return v;
        }
    }
}


