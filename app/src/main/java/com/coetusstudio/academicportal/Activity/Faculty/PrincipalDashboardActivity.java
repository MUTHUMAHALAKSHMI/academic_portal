package com.coetusstudio.academicportal.Activity.Faculty;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PrincipalDashboardActivity extends AppCompatActivity {

    private TextView tvTotalStudents, tvTotalFaculty, tvAvgAttendance, tvTotalDepts;
    private RecyclerView rvRecentActivities;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbarDashboard);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalFaculty = findViewById(R.id.tvTotalFaculty);
        tvAvgAttendance = findViewById(R.id.tvAvgAttendance);
        tvTotalDepts = findViewById(R.id.tvTotalDepts);
        rvRecentActivities = findViewById(R.id.rvRecentActivities);
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));

        rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();

        fetchDashboardData();
    }

    private void fetchDashboardData() {
        // Fetch Students Count
        rootRef.child("Student Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = 0;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    count += dept.getChildrenCount();
                }
                tvTotalStudents.setText(String.valueOf(count));
                tvTotalDepts.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Fetch Faculty Count
        rootRef.child("Faculty Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = 0;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    count += dept.getChildrenCount();
                }
                tvTotalFaculty.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Note: Avg Attendance calculation would require traversing all attendance records.
        // For a dashboard, we usually show an aggregated value if available.
        // For now, setting a placeholder or a simple calculation if possible.
        tvAvgAttendance.setText("82%"); // Placeholder for logic
    }
}
