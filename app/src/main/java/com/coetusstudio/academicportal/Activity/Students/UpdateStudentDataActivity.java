package com.coetusstudio.academicportal.Activity.Students;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateStudentDataActivity extends AppCompatActivity {

    private EditText name, email, roll, admission, enrollment, branch, semester, section, grade;
    private Button updateBtn;
    private String studentSection, studentRoll;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student_data);

        studentSection = getIntent().getStringExtra("studentSection");
        studentRoll = getIntent().getStringExtra("studentRollNumber");

        name = findViewById(R.id.updateName);
        email = findViewById(R.id.updateEmail);
        roll = findViewById(R.id.updateRoll);
        admission = findViewById(R.id.updateAdmission);
        enrollment = findViewById(R.id.updateEnrollment);
        branch = findViewById(R.id.updateBranch);
        semester = findViewById(R.id.updateSemester);
        section = findViewById(R.id.updateSection);
        grade = findViewById(R.id.updateGrade);
        updateBtn = findViewById(R.id.btnUpdateDetails);

        fetchDetails();

        updateBtn.setOnClickListener(v -> updateData());
    }

    private void fetchDetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").child(studentSection).child(studentRoll);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    name.setText(snapshot.child("studentName").getValue(String.class));
                    email.setText(snapshot.child("studentEmail").getValue(String.class));
                    roll.setText(snapshot.child("studentRollNumber").getValue(String.class));
                    admission.setText(snapshot.child("studentAdmissionNumber").getValue(String.class));
                    enrollment.setText(snapshot.child("studentEnrollmentNumber").getValue(String.class));
                    branch.setText(snapshot.child("studentBranch").getValue(String.class));
                    semester.setText(snapshot.child("studentSemester").getValue(String.class));
                    section.setText(snapshot.child("studentSection").getValue(String.class));
                    grade.setText(snapshot.child("studentGrade").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateData() {
        Map<String, Object> map = new HashMap<>();
        map.put("studentName", name.getText().toString());
        map.put("studentEmail", email.getText().toString());
        map.put("studentAdmissionNumber", admission.getText().toString());
        map.put("studentEnrollmentNumber", enrollment.getText().toString());
        map.put("studentBranch", branch.getText().toString());
        map.put("studentSemester", semester.getText().toString());
        map.put("studentSection", section.getText().toString());
        map.put("studentGrade", grade.getText().toString());

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data")
                .child(studentSection).child(studentRoll).updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error while updating", Toast.LENGTH_SHORT).show());
    }
}