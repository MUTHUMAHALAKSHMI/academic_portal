package com.coetusstudio.academicportal.Activity.Attendance;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadAttendanceActivity extends AppCompatActivity {

    private Spinner spinnerDate;
    private Button btnDownload;
    private String facultyDept, facultySubject;
    private List<String> dateList = new ArrayList<>();
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_attendance_faculty); // Reusing layout since it's identical

        setTitle("Download Attendance");
        spinnerDate = findViewById(R.id.spinnerDeleteAttendanceDate);
        btnDownload = findViewById(R.id.btnDeleteAttendanceNow);
        btnDownload.setText("DOWNLOAD ATTENDANCE");

        fetchFacultyDetails();

        btnDownload.setOnClickListener(v -> {
            if (spinnerDate.getSelectedItem() == null) return;
            String selectedDate = spinnerDate.getSelectedItem().toString();
            if (!selectedDate.equals("Select Date (Optional)")) {
                checkPermissionAndDownload(selectedDate);
            }
        });
    }

    private void fetchFacultyDetails() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        facultyDept = dept.getKey();
                        facultySubject = String.valueOf(dept.child(uid).child("facultySubject").getValue());
                        fetchDates();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchDates() {
        dateList.add("Select Date (Optional)");
        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(facultyDept).child(facultySubject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    dateList.add(ds.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(DownloadAttendanceActivity.this, android.R.layout.simple_spinner_dropdown_item, dateList);
                spinnerDate.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkPermissionAndDownload(String date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            generateCSV(date);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                generateCSV(date);
            }
        }
    }

    private void generateCSV(String date) {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(facultyDept).child(facultySubject).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder csvData = new StringBuilder();
                csvData.append("Roll Number,Attendance Status\n");
                for (DataSnapshot ds : snapshot.getChildren()) {
                    csvData.append(ds.getKey()).append(",").append(ds.child("atvalue").getValue()).append("\n");
                }
                saveFile(csvData.toString(), date);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveFile(String content, String date) {
        String fileName = "Attendance_" + facultySubject + "_" + date.replace(":", "-") + ".csv";
        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                java.io.File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(dir, fileName);
                fos = new java.io.FileOutputStream(file);
            }
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "File saved in Downloads folder", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}