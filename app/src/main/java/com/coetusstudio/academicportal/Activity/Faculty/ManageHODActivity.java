package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;

public class ManageHODActivity extends AppCompatActivity {

    private Spinner spinnerHODBranch, spinnerHODSemester;
    private Button btnManageHOD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_hod);

        spinnerHODBranch = findViewById(R.id.spinnerHODBranch);
        spinnerHODSemester = findViewById(R.id.spinnerHODSemester);
        btnManageHOD = findViewById(R.id.btnManageHOD);

        setupSpinners();

        btnManageHOD.setOnClickListener(v -> {
            String branch = spinnerHODBranch.getSelectedItem().toString();
            String semester = spinnerHODSemester.getSelectedItem().toString();

            if (branch.equals("Select Branch")) {
                Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
            } else if (semester.equals("Select Semester")) {
                Toast.makeText(this, "Please select semester", Toast.LENGTH_SHORT).show();
            } else {
                // Future implementation for HOD list
                // Intent intent = new Intent(ManageHODActivity.this, ManageHODListActivity.class);
                // intent.putExtra("branch", branch);
                // intent.putExtra("semester", semester);
                // startActivity(intent);
                Toast.makeText(this, "Feature coming soon for: " + branch + " - " + semester, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinners() {
        String[] branches = {
                "Select Branch",
                "COMPUTER SCIENCE AND ENGINEERING",
                "AI&DS ENGINEERING",
                "ELECTRONICS AND COMMUNICATION ENGINEERING",
                "ELECTRICAL AND ELECTRONICS ENGINEERING",
                "MECHANICAL ENGINEERING",
                "CIVIL ENGINEERING"
        };
        spinnerHODBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));

        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        spinnerHODSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));
    }
}
