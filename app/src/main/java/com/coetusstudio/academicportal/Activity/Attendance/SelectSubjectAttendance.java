package com.coetusstudio.academicportal.Activity.Attendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.coetusstudio.academicportal.R;
import com.coetusstudio.academicportal.databinding.ActivitySelectSubjectAttendanceBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectSubjectAttendance extends AppCompatActivity {

    ActivitySelectSubjectAttendanceBinding binding;
    String item_subject, studentSection, studentRollNumber, studentName, studentBranch;
    DatabaseReference dbSubjectRef;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectSubjectAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        final List<String> listSubject = new ArrayList<>();
        listSubject.add("Select Subject");
        
        // Adding requested subjects
        listSubject.add("C PROGRAMING");
        listSubject.add("ENGINEERING CHEMISTRY");
        listSubject.add("BASIC ELECTRICAL ENGINEERING");
        listSubject.add("ENGINEERING MATHEMATICS 1");

        ArrayAdapter<String> subjectArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSubject);
        subjectArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.selectSubTv.setAdapter(subjectArrayAdapter);

        binding.selectSubTv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item_subject = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dbSubjectRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                    for (DataSnapshot studentSnap : sectionSnap.getChildren()) {
                        Object emailObj = studentSnap.child("studentEmail").getValue();
                        String email = emailObj != null ? String.valueOf(emailObj).trim() : null;
                        
                        if (email != null && email.equalsIgnoreCase(userEmail)) {
                            studentName = String.valueOf(studentSnap.child("studentName").getValue());
                            studentSection = String.valueOf(studentSnap.child("studentSection").getValue());
                            studentRollNumber = String.valueOf(studentSnap.child("studentRollNumber").getValue());
                            studentBranch = String.valueOf(studentSnap.child("studentBranch").getValue());
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }

                if (found && studentBranch != null && !studentBranch.equalsIgnoreCase("null")) {
                    dbSubjectRef.child(studentBranch.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Don't clear the list to keep our manually added subjects
                            for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                Object subObj = dsp.child("facultySubject").getValue();
                                if (subObj != null) {
                                    String sub = String.valueOf(subObj);
                                    if (!listSubject.contains(sub)) {
                                        listSubject.add(sub);
                                    }
                                }
                            }
                            subjectArrayAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item_subject == null || item_subject.equals("Select Subject")) {
                    Toast.makeText(SelectSubjectAttendance.this, "Please select a subject", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(SelectSubjectAttendance.this, AttendanceActivity.class);
                intent.putExtra("subjectName", item_subject);
                intent.putExtra("studentSection", studentSection);
                intent.putExtra("studentRollNumber", studentRollNumber);
                intent.putExtra("studentName", studentName);
                startActivity(intent);
            }
        });
    }
}
