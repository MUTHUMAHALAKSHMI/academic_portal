package com.coetusstudio.academicportal.Activity.Attendance;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.Model.StudentDetails;
import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeAttendanceActivity extends AppCompatActivity {

    private String date, type, facultyDept, facultySubject;
    private boolean isUpdate = false;
    private RecyclerView recyclerView;
    private Button btnSubmit;
    private List<StudentDetails> studentList = new ArrayList<>();
    private AttendanceAdapter adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        date = getIntent().getStringExtra("date");
        type = getIntent().getStringExtra("type");
        isUpdate = getIntent().getBooleanExtra("isUpdate", false);

        recyclerView = findViewById(R.id.rvTakeAttendance);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnSubmit = findViewById(R.id.btnSubmitAttendance);

        fetchFacultyDetails();

        btnSubmit.setOnClickListener(v -> {
            if (studentList.isEmpty()) {
                Toast.makeText(this, "No students found to mark attendance", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog();
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
                    fetchStudents();
                } else {
                    Toast.makeText(TakeAttendanceActivity.this, "Faculty profile not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchStudents() {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                // Iterate through all nodes (sections) in Student Data
                for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                    for (DataSnapshot studentSnap : sectionSnap.getChildren()) {
                        String sBranch = String.valueOf(studentSnap.child("studentBranch").getValue());
                        // Filter students by the faculty's branch/department
                        if (sBranch != null && sBranch.equalsIgnoreCase(facultyDept)) {
                            StudentDetails student = studentSnap.getValue(StudentDetails.class);
                            if (student != null) {
                                studentList.add(student);
                            }
                        }
                    }
                }
                
                if (studentList.isEmpty()) {
                    Toast.makeText(TakeAttendanceActivity.this, "No students found for: " + facultyDept, Toast.LENGTH_LONG).show();
                }
                
                if (isUpdate) {
                    fetchExistingAttendance();
                } else {
                    adapter = new AttendanceAdapter(studentList);
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchExistingAttendance() {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(facultyDept).child(facultySubject).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter = new AttendanceAdapter(studentList);
                for (int i = 0; i < studentList.size(); i++) {
                    String roll = studentList.get(i).getStudentRollNumber();
                    if (snapshot.hasChild(roll)) {
                        String val = String.valueOf(snapshot.child(roll).child("atvalue").getValue());
                        if (val.startsWith("1")) {
                            adapter.setCheckState(i, true);
                        }
                    }
                }
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation!");
        builder.setMessage("Do you want to Submit?");
        builder.setPositiveButton("YES", (dialog, which) -> submitAttendance());
        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void submitAttendance() {
        if (facultyDept == null || facultySubject == null || date == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet").child(facultyDept).child(facultySubject).child(date);
        
        int presentCount = 0;
        int absentCount = 0;

        for (int i = 0; i < studentList.size(); i++) {
            StudentDetails student = studentList.get(i);
            boolean isChecked = adapter.getCheckState(i);
            
            String value;
            if (isUpdate) {
                value = isChecked ? "1/1" : "0/1";
            } else {
                if (type != null && type.contains("Present")) {
                    value = isChecked ? "1/1" : "0/1";
                } else {
                    value = isChecked ? "0/1" : "1/1";
                }
            }
            
            if (value.equals("1/1")) presentCount++; else absentCount++;

            Map<String, String> data = new HashMap<>();
            data.put("atvalue", value);
            ref.child(student.getStudentRollNumber()).setValue(data);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Submitted Successfully!");
        builder.setMessage("Present=" + presentCount + " Absent=" + absentCount + " Total Student=" + studentList.size());
        builder.setPositiveButton("OK", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private List<StudentDetails> students;
        private boolean[] checkStates;

        public AttendanceAdapter(List<StudentDetails> students) {
            this.students = students;
            this.checkStates = new boolean[students.size()];
        }

        public void setCheckState(int position, boolean state) {
            if (position < checkStates.length) {
                checkStates[position] = state;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_attendance_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvRoll.setText(students.get(position).getStudentRollNumber());
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(checkStates[position]);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkStates[position] = isChecked);
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        public boolean getCheckState(int position) {
            if (position < checkStates.length) {
                return checkStates[position];
            }
            return false;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRoll;
            CheckBox checkBox;

            ViewHolder(View itemView) {
                super(itemView);
                tvRoll = itemView.findViewById(R.id.tvStudentRoll);
                checkBox = itemView.findViewById(R.id.cbAttendance);
            }
        }
    }
}
