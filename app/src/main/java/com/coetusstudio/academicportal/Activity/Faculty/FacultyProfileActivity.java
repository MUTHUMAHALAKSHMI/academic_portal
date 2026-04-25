package com.coetusstudio.academicportal.Activity.Faculty;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FacultyProfileActivity extends AppCompatActivity {

    private ImageView btnBack, passVisible;
    private CircleImageView profileImage;
    private TextView name, email, id, subject, subjectCode, department, password;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_profile);

        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnFacultyBack);
        passVisible = findViewById(R.id.facultyPassVisible);
        profileImage = findViewById(R.id.facultyImageProfile);
        name = findViewById(R.id.facultyNameProfile);
        email = findViewById(R.id.facultyEmailProfile);
        id = findViewById(R.id.facultyIdProfile);
        subject = findViewById(R.id.facultySubjectProfile);
        subjectCode = findViewById(R.id.facultySubjectCodeProfile);
        department = findViewById(R.id.facultyDeptProfile);
        password = findViewById(R.id.facultyPasswordProfile);

        password.setTransformationMethod(PasswordTransformationMethod.getInstance());

        btnBack.setOnClickListener(v -> finish());

        passVisible.setOnClickListener(v -> {
            if (isPasswordVisible) {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passVisible.setImageResource(R.drawable.ic_baseline_visibility);
                isPasswordVisible = false;
            } else {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passVisible.setImageResource(R.drawable.ic_baseline_visibility_off);
                isPasswordVisible = true;
            }
        });

        loadUserData();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        DataSnapshot user = dept.child(uid);
                        
                        String fName = user.child("facultyName").getValue(String.class);
                        String fEmail = user.child("facultyEmail").getValue(String.class);
                        String fId = user.child("facultyId").getValue(String.class);
                        String fSubject = user.child("facultySubject").getValue(String.class);
                        String fSubjectCode = user.child("facultySubjectCode").getValue(String.class);
                        String fDept = user.child("facultyDept").getValue(String.class);
                        String fPass = user.child("facultyPassword").getValue(String.class);
                        String fImage = user.child("facultyImage").getValue(String.class);

                        name.setText(fName != null ? fName : "Rajasubramaniyan");
                        email.setText(fEmail != null ? fEmail : "rajasubramaniyan123@gmail.com");
                        id.setText(fId != null ? fId : "SMTECF00123");
                        subject.setText(fSubject != null ? fSubject : "AI&DS");
                        subjectCode.setText(fSubjectCode != null ? fSubjectCode : "CS0034");
                        department.setText(fDept != null ? fDept : "COMPUTER SCIENCE");
                        
                        if (fPass != null) {
                            password.setText(fPass);
                        } else {
                            password.setText("********");
                        }

                        if (fImage != null && !fImage.isEmpty()) {
                            Glide.with(FacultyProfileActivity.this).load(fImage).placeholder(R.drawable.manimg).into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
