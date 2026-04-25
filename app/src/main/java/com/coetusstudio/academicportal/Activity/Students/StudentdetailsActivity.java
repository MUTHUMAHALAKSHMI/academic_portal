package com.coetusstudio.academicportal.Activity.Students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.R;
import com.coetusstudio.academicportal.databinding.ActivityStudentdetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class StudentdetailsActivity extends AppCompatActivity {

    ActivityStudentdetailsBinding binding;
    FirebaseAuth auth;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private String userSectionPath = ""; // To store the section path for updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentdetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStudentBack.setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        loadStudentData(uid);

        // Check if we should automatically open in edit mode (from "Edit Profile" card)
        boolean openInEditMode = getIntent().getBooleanExtra("openEditMode", false);
        if (openInEditMode) {
            toggleEditMode(true);
        }

        binding.btnEditProfile.setOnClickListener(v -> toggleEditMode(true));

        binding.btnSaveProfile.setOnClickListener(v -> saveProfileChanges(uid));
    }

    private void loadStudentData(String uid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                    if (sectionSnap.hasChild(uid)) {
                        try {
                            userSectionPath = sectionSnap.getKey();
                            DataSnapshot user = sectionSnap.child(uid);
                            
                            binding.studentName.setText(String.valueOf(user.child("studentName").getValue()));
                            binding.studentEmailId.setText(String.valueOf(user.child("studentEmail").getValue()));
                            binding.studentRollNumber.setText(String.valueOf(user.child("studentRollNumber").getValue()));
                            binding.studentAdmissionNumber.setText(String.valueOf(user.child("studentAdmissionNumber").getValue()));
                            binding.studentEnroolmentNumber.setText(String.valueOf(user.child("studentEnrollmentNumber").getValue()));
                            binding.studentBranch.setText(String.valueOf(user.child("studentBranch").getValue()));
                            binding.studentSemester.setText(String.valueOf(user.child("studentSemester").getValue()));
                            binding.studentSection.setText(String.valueOf(user.child("studentSection").getValue()));
                            binding.studentGrade.setText(String.valueOf(user.child("studentGrade").getValue()));
                            
                            if (user.hasChild("studentPhone")) {
                                binding.studentPhone.setText(String.valueOf(user.child("studentPhone").getValue()));
                            }
                            if (user.hasChild("studentAddress")) {
                                binding.studentAddress.setText(String.valueOf(user.child("studentAddress").getValue()));
                            }

                            String password = String.valueOf(user.child("studentPassword").getValue());
                            String image = String.valueOf(user.child("studentImage").getValue());
                            
                            Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.manimg).error(R.drawable.manimg).into(binding.studentImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentdetailsActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditMode(boolean editable) {
        binding.studentEmailId.setEnabled(editable);
        binding.studentPhone.setEnabled(editable);
        binding.studentAddress.setEnabled(editable);
        
        binding.btnSaveProfile.setVisibility(editable ? View.VISIBLE : View.GONE);
        binding.btnEditProfile.setVisibility(editable ? View.GONE : View.VISIBLE);

        if (editable) {
            binding.studentEmailId.requestFocus();
            Toast.makeText(this, "Edit Mode Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileChanges(String uid) {
        String newEmail = binding.studentEmailId.getText().toString().trim();
        String newPhone = binding.studentPhone.getText().toString().trim();
        String newAddress = binding.studentAddress.getText().toString().trim();

        if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            binding.studentEmailId.setError("Enter a valid email");
            return;
        }

        if (!newPhone.isEmpty() && newPhone.length() < 10) {
            binding.studentPhone.setError("Enter a valid phone number");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("studentEmail", newEmail);
        updates.put("studentPhone", newPhone);
        updates.put("studentAddress", newAddress);

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference()
                .child("Student Data").child(userSectionPath).child(uid);

        ref.updateChildren(updates).addOnSuccessListener(unused -> {
            toggleEditMode(false);
            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
