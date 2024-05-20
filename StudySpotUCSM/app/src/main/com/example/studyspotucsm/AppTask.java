package com.example.studyspotucsm;

import android.content.Context;
import androidx.core.content.ContextCompat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AppTask implements Comparable<AppTask> {
    private int ID;
    private String task;
    private String Ubicacion;
    private LocalTime from;
    private LocalTime to;
    private String color;
    private String building;
    private String room;
    private String professor;
    private String location;
    private String tipoClase;
    private String recurrence = "weekly";
    private final transient DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public AppTask() {
        // Constructor vacío necesario para Firebase
    }
    public AppTask(AppTask source) {
        // Asumiendo que el ID no se copia porque debe ser único para cada tarea nueva
        this.task = source.task;
        this.Ubicacion = source.Ubicacion;
        this.from = source.from; // LocalTime es inmutable, está bien copiar directamente
        this.to = source.to;     // LocalTime es inmutable, está bien copiar directamente
        this.color = source.color;
        this.building = source.building;
        this.room = source.room;
        this.professor = source.professor;
        this.location = source.location;
        this.tipoClase = source.tipoClase;
        this.recurrence = source.recurrence;
    }
    public String getTipoClase() {
        return tipoClase;
    }

    public void setTipoClase(String tipoClase) {
        this.tipoClase = tipoClase;
    }

    public int getId() {
        return ID;
    }


    public void setId(int ID) {
        this.ID = ID;
    }
    public String getRecurrence() {
        return recurrence;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getFromToString() {
        return (from != null) ? from.format(formatter) : "";
    }

    public void setFrom(String from) {
        this.from = LocalTime.parse(from, formatter);
    }

    public LocalTime getFrom() {
        return from;
    }

    public String getToToString() {
        return (to != null) ? to.format(formatter) : "";
    }

    public void setTo(String to) {
        this.to = LocalTime.parse(to, formatter);
    }

    public LocalTime getTo() {
        return to;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public String getUbicacion() {
        return Ubicacion;
    }

    public void setUbicacion(String Ubicacion) {
        this.Ubicacion = Ubicacion;
    }


    public int getColor(Context context) {
        int colorId = R.color.default_color; // Un color por defecto en caso de que no se encuentre el color especificado.
        switch (this.color.toLowerCase()) {
            case "blue":
                colorId = R.color.blue;
                break;
            case "green":
                colorId = R.color.green;
                break;
            case "red":
                colorId = R.color.red;
                break;
            case "yellow":
                colorId = R.color.yellow;
                break;
            case "orange":
                colorId = R.color.orange;
                break;
            case "purple":
                colorId = R.color.purple;
                break;
            case "grey":
                colorId = R.color.grey;
                break;
        }
        return ContextCompat.getColor(context, colorId);
    }
    // Métodos getters y setters para cada uno
    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("task", task);
        result.put("from", getFromToString());
        result.put("to", getToToString());
        result.put("color", color);
        result.put("building", building);
        result.put("Ubicacion",Ubicacion);
        result.put("room", room);
        result.put("professor", professor);
        result.put("tipoClase", tipoClase);
        return result;
    }

    @Override
    public int compareTo(AppTask other) {
        if (this.from == null || other.getFrom() == null) {
            return 0;
        }
        return this.from.compareTo(other.getFrom());
    }
}
