package com.example.studyspotucsm;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Database {

    private DatabaseReference database;
    private String userId;

    public Database(String userId) {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.userId = userId; // ID del usuario actual
    }

    public void addTask(AppTask t, String date) {
        String key = database.child("users").child(userId).child("tasks").child(date).push().getKey();
        Map<String, Object> taskValues = t.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + userId + "/tasks/" + date + "/" + key, taskValues);
        database.updateChildren(childUpdates);
    }

    public void updateTask(AppTask t, String date) {
        String key = String.valueOf(t.getId());
        Map<String, Object> taskValues = t.toMap();
        database.child("users").child(userId).child("tasks").child(date).child(key).updateChildren(taskValues);
    }

    public void deleteTask(String taskId, String date) {
        database.child("users").child(userId).child("tasks").child(date).child(taskId).removeValue();
    }

    public void getAllTasks(String date, OnTaskReceivedListener listener) {
        DatabaseReference tasksRef = database.child("users").child(userId).child("tasks").child(date);
        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<AppTask> tasks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AppTask task = snapshot.getValue(AppTask.class);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
                listener.onTaskReceived(tasks);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public interface OnTaskReceivedListener {
        void onTaskReceived(ArrayList<AppTask> tasks);
    }
}
