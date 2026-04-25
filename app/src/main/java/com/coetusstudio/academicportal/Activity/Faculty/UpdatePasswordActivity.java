package com.coetusstudio.academicportal.Activity.Faculty;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText etUsername, etNewPassword;
    private Button btnUpdatePassword, btnBack;
    private DatabaseReference reference;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        Toolbar toolbar = findViewById(R.id.toolbarUpdatePassword);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnBack = findViewById(R.id.btnBack);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Passwords");

        btnUpdatePassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Required");
                return;
            }
            if (password.isEmpty()) {
                etNewPassword.setError("Required");
                return;
            }

            reference.child(username).setValue(password).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                etUsername.setText("");
                etNewPassword.setText("");
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
