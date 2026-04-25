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

public class HODProfileActivity extends AppCompatActivity {

    private ImageView btnBack, passVisible;
    private CircleImageView profileImage;
    private TextView name, email, id, position, school, password;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_profile);

        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnHodBack);
        passVisible = findViewById(R.id.hodPassVisible);
        profileImage = findViewById(R.id.hodImageProfile);
        name = findViewById(R.id.hodNameProfile);
        email = findViewById(R.id.hodEmailProfile);
        id = findViewById(R.id.hodIdProfile);
        position = findViewById(R.id.hodPositionProfile);
        school = findViewById(R.id.hodSchoolProfile);
        password = findViewById(R.id.hodPasswordProfile);

        // Mask password by default
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

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        DataSnapshot user = dept.child(uid);
                        
                        // Fetching values from Firebase
                        String hodName = user.child("hodName").getValue(String.class);
                        String hodEmail = user.child("hodEmail").getValue(String.class);
                        String hodId = user.child("hodId").getValue(String.class);
                        String hodPosition = user.child("hodPosition").getValue(String.class);
                        String hodSchool = user.child("hodSchool").getValue(String.class);
                        String hodPassword = user.child("hodPassword").getValue(String.class);
                        String hodImage = user.child("hodImage").getValue(String.class);

                        // If value is null in Firebase, use the provided defaults
                        name.setText(hodName != null ? hodName : "RAJASUBRAMANIYAN");
                        email.setText(hodEmail != null ? hodEmail : "rajasubramaniyan123@gmail.com");
                        id.setText(hodId != null ? hodId : "SMTECHOD1234");
                        position.setText(hodPosition != null ? hodPosition : "HOD");
                        school.setText(hodSchool != null ? hodSchool : "ST.MOTHER TERESA ENGINEERING COLLEGE");
                        
                        if (hodPassword != null) {
                            password.setText(hodPassword);
                        } else {
                            password.setText("rajasubramaniyan123");
                        }

                        if (hodImage != null && !hodImage.isEmpty()) {
                            Glide.with(HODProfileActivity.this).load(hodImage).placeholder(R.drawable.manimg).into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
