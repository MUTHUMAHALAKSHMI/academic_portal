package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.coetusstudio.academicportal.R;

public class AdministratorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnCreateBranch, btnCreateSemester, btnCreateSection, btnCreateSubject, btnCreateSessionalTitle, btnCreatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrator);

        toolbar = findViewById(R.id.toolbarAdministrator);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnCreateBranch = findViewById(R.id.btnCreateBranch);
        btnCreateSemester = findViewById(R.id.btnCreateSemester);
        btnCreateSection = findViewById(R.id.btnCreateSection);
        btnCreateSubject = findViewById(R.id.btnCreateSubject);
        btnCreateSessionalTitle = findViewById(R.id.btnCreateSessionalTitle);
        btnCreatePassword = findViewById(R.id.btnCreatePassword);

        btnCreateBranch.setOnClickListener(v -> startActivity(new Intent(this, CreateBranchActivity.class)));
        btnCreateSemester.setOnClickListener(v -> startActivity(new Intent(this, CreateSemesterActivity.class)));
        btnCreateSection.setOnClickListener(v -> startActivity(new Intent(this, CreateSectionActivity.class)));
        btnCreateSubject.setOnClickListener(v -> startActivity(new Intent(this, CreateSubjectActivity.class)));
        btnCreateSessionalTitle.setOnClickListener(v -> startActivity(new Intent(this, CreateSessionalTitleActivity.class)));
        btnCreatePassword.setOnClickListener(v -> startActivity(new Intent(this, UpdatePasswordActivity.class)));
    }
}
