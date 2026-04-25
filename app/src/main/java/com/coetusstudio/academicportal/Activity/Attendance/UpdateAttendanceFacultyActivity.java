package com.coetusstudio.academicportal.Activity.Attendance;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UpdateAttendanceFacultyActivity extends AppCompatActivity {

    private Spinner spinnerDate;
    private Button btnUpdate;
    private String facultyDept, facultySubject;
    private List<String> dateList = new ArrayList<>();
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_attendance_faculty);

        spinnerDate = findViewById(R.id.spinnerUpdateAttendanceDate);
        btnUpdate = findViewById(R.id.btnGoToUpdateAttendance);

        fetchFacultyDetails();

        btnUpdate.setOnClickListener(v -> {
            if (spinnerDate.getSelectedItem() == null) {
                Toast.makeText(this, "Wait for dates to load...", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDate = spinnerDate.getSelectedItem().toString();
            if (selectedDate.equals("Select Date (Optional)")) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, TakeAttendanceActivity.class);
                intent.putExtra("date", selectedDate);
                intent.putExtra("dept", facultyDept);
                intent.putExtra("subject", facultySubject);
                intent.putExtra("isUpdate", true);
                startActivity(intent);
            }
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
                    fetchDates();
                } else {
                    Toast.makeText(UpdateAttendanceFacultyActivity.this, "Faculty profile not found", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchDates() {
        if (facultyDept == null || facultySubject == null) return;

        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(facultyDept.trim()).child(facultySubject.trim()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                
                dateList.clear();
                dateList.add("Select Date (Optional)");
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        dateList.add(ds.getKey());
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdateAttendanceFacultyActivity.this, android.R.layout.simple_spinner_dropdown_item, dateList);
                spinnerDate.setAdapter(adapter);
                
                if (dateList.size() <= 1) {
                    Toast.makeText(UpdateAttendanceFacultyActivity.this, "No records found for " + facultySubject, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
