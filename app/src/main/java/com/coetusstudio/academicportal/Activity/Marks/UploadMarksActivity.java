package com.coetusstudio.academicportal.Activity.Marks;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.Model.SessionalMarks;
import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UploadMarksActivity extends AppCompatActivity {

    private Spinner spinnerResultType, spinnerRollNumber, spinnerSub1, spinnerSub2, spinnerSub3, spinnerSub4, spinnerSub5;
    private EditText etMaxMarks, etMarks1, etMarks2, etMarks3, etMarks4, etMarks5;
    private Button btnUploadMarks;
    private ProgressDialog pd;

    private DatabaseReference reference, facultyRef, studentRef;
    private String facultyDept = "";
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_marks);

        pd = new ProgressDialog(this);
        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("SessionalMarks");
        facultyRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        studentRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data");

        initViews();
        setupSpinners();
        getFacultyDetails();

        btnUploadMarks.setOnClickListener(v -> validateData());
    }

    private void initViews() {
        spinnerResultType = findViewById(R.id.spinnerResultType);
        spinnerRollNumber = findViewById(R.id.spinnerRollNumber);
        spinnerSub1 = findViewById(R.id.spinnerSub1);
        spinnerSub2 = findViewById(R.id.spinnerSub2);
        spinnerSub3 = findViewById(R.id.spinnerSub3);
        spinnerSub4 = findViewById(R.id.spinnerSub4);
        spinnerSub5 = findViewById(R.id.spinnerSub5);

        etMaxMarks = findViewById(R.id.etMaxMarks);
        etMarks1 = findViewById(R.id.etMarks1);
        etMarks2 = findViewById(R.id.etMarks2);
        etMarks3 = findViewById(R.id.etMarks3);
        etMarks4 = findViewById(R.id.etMarks4);
        etMarks5 = findViewById(R.id.etMarks5);

        btnUploadMarks = findViewById(R.id.btnUploadMarks);
    }

    private void setupSpinners() {
        // Result Types updated as requested
        String[] resultTypes = {"INTERNAL ASSESMENT 1", "INTERNAL ASSESMENT 2", "MODEL EXAM"};
        spinnerResultType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resultTypes));

        // Subjects (Example - could be dynamic from DB)
        String[] subjects = {"Select Subject", "C PROGRAMING", "ENGINEERING CHEMISTRY", "BASIC ELECTRICAL ENGINEERING", "ENGINEERING MATHEMATICS 1", "PROFESSIONAL COMMUNICATION"};
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSub1.setAdapter(subAdapter);
        spinnerSub2.setAdapter(subAdapter);
        spinnerSub3.setAdapter(subAdapter);
        spinnerSub4.setAdapter(subAdapter);
        spinnerSub5.setAdapter(subAdapter);
    }

    private void getFacultyDetails() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        facultyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        facultyDept = dept.getKey();
                        loadStudents(facultyDept);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStudents(String dept) {
        studentRef.child(dept).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> rollNumbers = new ArrayList<>();
                rollNumbers.add("Select Roll Number");
                for (DataSnapshot student : snapshot.getChildren()) {
                    // FIXED: Handle Long to String conversion for Roll Number
                    String roll = String.valueOf(student.child("studentRollNumber").getValue());
                    if (roll != null && !roll.equals("null")) rollNumbers.add(roll);
                }
                spinnerRollNumber.setAdapter(new ArrayAdapter<>(UploadMarksActivity.this, android.R.layout.simple_spinner_dropdown_item, rollNumbers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void validateData() {
        String roll = spinnerRollNumber.getSelectedItem().toString();
        String max = etMaxMarks.getText().toString();
        
        if (roll.equals("Select Roll Number")) {
            Toast.makeText(this, "Please select roll number", Toast.LENGTH_SHORT).show();
        } else if (max.isEmpty()) {
            etMaxMarks.setError("Required");
        } else {
            uploadData();
        }
    }

    private void uploadData() {
        pd.setMessage("Uploading Marks...");
        pd.show();

        String resultType = spinnerResultType.getSelectedItem().toString();
        String roll = spinnerRollNumber.getSelectedItem().toString();
        
        SessionalMarks marks = new SessionalMarks(
                resultType, roll, "", etMaxMarks.getText().toString(),
                spinnerSub1.getSelectedItem().toString(), etMarks1.getText().toString(),
                spinnerSub2.getSelectedItem().toString(), etMarks2.getText().toString(),
                spinnerSub3.getSelectedItem().toString(), etMarks3.getText().toString(),
                spinnerSub4.getSelectedItem().toString(), etMarks4.getText().toString(),
                spinnerSub5.getSelectedItem().toString(), etMarks5.getText().toString()
        );

        reference.child(facultyDept).child(roll).push().setValue(marks).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(UploadMarksActivity.this, "Marks Uploaded Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(UploadMarksActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
