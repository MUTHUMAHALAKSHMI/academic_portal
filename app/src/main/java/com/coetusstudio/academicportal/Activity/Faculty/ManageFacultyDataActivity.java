package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;

public class ManageFacultyDataActivity extends AppCompatActivity {

    private Spinner spinnerFacultyBranch, spinnerFacultySemester;
    private Button btnManageFaculty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_faculty_data);

        spinnerFacultyBranch = findViewById(R.id.spinnerFacultyBranch);
        spinnerFacultySemester = findViewById(R.id.spinnerFacultySemester);
        btnManageFaculty = findViewById(R.id.btnManageFaculty);

        setupSpinners();

        btnManageFaculty.setOnClickListener(v -> {
            String branch = spinnerFacultyBranch.getSelectedItem().toString();
            String semester = spinnerFacultySemester.getSelectedItem().toString();

            if (branch.equals("Select Branch")) {
                Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
            } else if (semester.equals("Select Semester")) {
                Toast.makeText(this, "Please select semester", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(ManageFacultyDataActivity.this, ManageFacultyListActivity.class);
                intent.putExtra("branch", branch);
                intent.putExtra("semester", semester);
                startActivity(intent);
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
        spinnerFacultyBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));

        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        spinnerFacultySemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));
    }
}
