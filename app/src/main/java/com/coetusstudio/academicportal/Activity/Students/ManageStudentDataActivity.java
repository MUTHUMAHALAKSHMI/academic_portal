package com.coetusstudio.academicportal.Activity.Students;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;

public class ManageStudentDataActivity extends AppCompatActivity {

    private Spinner spinnerSemester, spinnerSection;
    private Button btnViewDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_student_data);

        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerSection = findViewById(R.id.spinnerSection);
        btnViewDetails = findViewById(R.id.btnViewDetails);

        setupSpinners();

        btnViewDetails.setOnClickListener(v -> {
            String semester = spinnerSemester.getSelectedItem().toString();
            String section = spinnerSection.getSelectedItem().toString();

            if (semester.equals("Select Semester")) {
                Toast.makeText(this, "Please select semester", Toast.LENGTH_SHORT).show();
            } else if (section.equals("Select Section")) {
                Toast.makeText(this, "Please select section", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(ManageStudentDataActivity.this, ViewStudentDataActivity.class);
                intent.putExtra("semester", semester);
                intent.putExtra("section", section);
                startActivity(intent);
            }
        });
    }

    private void setupSpinners() {
        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        spinnerSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));

        String[] sections = {"Select Section", "A", "B", "C", "D", "E"};
        spinnerSection.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sections));
    }
}