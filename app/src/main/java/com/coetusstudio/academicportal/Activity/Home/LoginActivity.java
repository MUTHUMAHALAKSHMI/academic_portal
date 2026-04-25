package com.coetusstudio.academicportal.Activity.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.coetusstudio.academicportal.Activity.Faculty.FacultyMainActivity;
import com.coetusstudio.academicportal.Activity.Faculty.HODMainActivity;
import com.coetusstudio.academicportal.Activity.Faculty.PrincipalMainActivity;
import com.coetusstudio.academicportal.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Identifying user role...");
        progressDialog.setCancelable(false);

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailStudent.getEditText().getText().toString().trim();
                String password = binding.passwordStudent.getEditText().getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter credentials", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();
                
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserRole(email);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void checkUserRole(final String email) {
        if (auth.getCurrentUser() == null) return;
        final String uid = auth.getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();

        // Check Principal/Admin first
        rootRef.child("principal and Admin Data").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    proceed(email, "principal", "admin");
                } else {
                    checkStudent(email, uid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void checkStudent(final String email, final String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        rootRef.child("Student Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot section : snapshot.getChildren()) {
                    if (section.hasChild(uid)) {
                        proceed(email, "student", section.getKey());
                        return;
                    }
                }
                checkFaculty(email, uid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void checkFaculty(final String email, final String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        rootRef.child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot section : snapshot.getChildren()) {
                    if (section.hasChild(uid)) {
                        proceed(email, "faculty", section.getKey());
                        return;
                    }
                }
                checkHOD(email, uid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void checkHOD(final String email, final String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        rootRef.child("Hod Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot section : snapshot.getChildren()) {
                    if (section.hasChild(uid)) {
                        proceed(email, "hod", section.getKey());
                        return;
                    }
                }
                progressDialog.dismiss();
                auth.signOut();
                Toast.makeText(LoginActivity.this, "Access Denied: Record not found.", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void proceed(String email, String role, String dept) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        
        Intent intent;
        if ("principal".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, PrincipalMainActivity.class);
        } else if ("hod".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, HODMainActivity.class);
        } else if ("faculty".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, FacultyMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        
        intent.putExtra("confirmEmail", email);
        intent.putExtra("userRole", role);
        intent.putExtra("userDept", dept);
        startActivity(intent);
        finish();
    }
}
