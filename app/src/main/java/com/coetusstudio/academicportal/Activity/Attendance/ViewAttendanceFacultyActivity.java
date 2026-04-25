package com.coetusstudio.academicportal.Activity.Attendance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceFacultyActivity extends AppCompatActivity {

    private Spinner spinnerDate;
    private Button btnViewDay, btnCollective;
    private String facultyDept, facultySubject;
    private List<String> dateList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_faculty);

        spinnerDate = findViewById(R.id.spinnerAttendanceDate);
        btnViewDay = findViewById(R.id.btnViewDayAttendance);
        btnCollective = findViewById(R.id.btnCollectiveAttendance);

        // Set spinner background to white to match screenshot
        spinnerDate.setBackgroundColor(Color.WHITE);

        // Initialize with default item
        dateList.add("Select Date (Optional)");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDate.setAdapter(adapter);

        fetchFacultyDetails();

        btnViewDay.setOnClickListener(v -> {
            if (spinnerDate.getSelectedItem() == null) {
                Toast.makeText(this, "Wait for dates to load...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String selectedDate = spinnerDate.getSelectedItem().toString();
            if (selectedDate.equals("Select Date (Optional)")) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, DayWiseAttendanceActivity.class);
                intent.putExtra("date", selectedDate);
                intent.putExtra("dept", facultyDept);
                intent.putExtra("subject", facultySubject);
                startActivity(intent);
            }
        });

        btnCollective.setOnClickListener(v -> {
            if (facultyDept == null || facultySubject == null) {
                Toast.makeText(this, "Still fetching faculty details...", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CollectiveAttendanceActivity.class);
            intent.putExtra("dept", facultyDept);
            intent.putExtra("subject", facultySubject);
            startActivity(intent);
        });
    }

    private void fetchFacultyDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        String email = currentUser.getEmail();
        
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    for (DataSnapshot faculty : dept.getChildren()) {
                        String fEmail = faculty.child("facultyEmail").getValue(String.class);
                        if (fEmail != null && fEmail.equalsIgnoreCase(email)) {
                            facultyDept = dept.getKey();
                            facultySubject = faculty.child("facultySubject").getValue(String.class);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                
                if (found && facultyDept != null && facultySubject != null) {
                    Toast.makeText(ViewAttendanceFacultyActivity.this, "Found Profile: " + facultySubject + " in " + facultyDept, Toast.LENGTH_LONG).show();
                    fetchDates();
                } else {
                    Toast.makeText(ViewAttendanceFacultyActivity.this, "Faculty profile not found for: " + email, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchDates() {
        DatabaseReference dateRef = FirebaseDatabase.getInstance(DB_URL).getReference()
                .child("AttendenRecordSheet")
                .child(facultyDept)
                .child(facultySubject);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                
                dateList.clear();
                dateList.add("Select Date (Optional)");
                
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        dateList.add(ds.getKey());
                    }
                    Toast.makeText(ViewAttendanceFacultyActivity.this, "Dates loaded successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    String path = "AttendenRecordSheet/" + facultyDept + "/" + facultySubject;
                    Toast.makeText(ViewAttendanceFacultyActivity.this, "No records found at: " + path, Toast.LENGTH_LONG).show();
                }
                
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAttendanceFacultyActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
