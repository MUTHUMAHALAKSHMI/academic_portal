package com.coetusstudio.academicportal.Activity.Attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;

public class ManageAttendanceActivity extends AppCompatActivity {

    Button btnMarkAttendance, btnViewAttendance, btnUpdateAttendance, btnDeleteAttendance, btnDownloadAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance);

        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        btnUpdateAttendance = findViewById(R.id.btnUpdateAttendance);
        btnDeleteAttendance = findViewById(R.id.btnDeleteAttendance);
        btnDownloadAttendance = findViewById(R.id.btnDownloadAttendance);

        btnMarkAttendance.setOnClickListener(v -> {
            startActivity(new Intent(ManageAttendanceActivity.this, MarkAttendanceActivity.class));
        });

        btnViewAttendance.setOnClickListener(v -> {
             startActivity(new Intent(ManageAttendanceActivity.this, ViewAttendanceFacultyActivity.class));
        });

        btnUpdateAttendance.setOnClickListener(v -> {
             startActivity(new Intent(ManageAttendanceActivity.this, UpdateAttendanceFacultyActivity.class));
        });

        btnDeleteAttendance.setOnClickListener(v -> {
             startActivity(new Intent(ManageAttendanceActivity.this, DeleteAttendanceFacultyActivity.class));
        });

        btnDownloadAttendance.setOnClickListener(v -> {
             startActivity(new Intent(ManageAttendanceActivity.this, DownloadAttendanceActivity.class));
        });
    }
}