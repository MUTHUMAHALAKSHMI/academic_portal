package com.coetusstudio.academicportal.Activity.Attendance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.R;

import java.util.Calendar;

public class MarkAttendanceActivity extends AppCompatActivity {

    private EditText etDate;
    private Spinner spinnerType;
    private Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        etDate = findViewById(R.id.etAttendanceDate);
        spinnerType = findViewById(R.id.spinnerAttendanceType);
        btnProceed = findViewById(R.id.btnProceedAttendance);

        String[] types = {"Select Type", "Select Present Student Only", "Select Absent Student Only"};
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String date = dayOfMonth + "-" + (month1 + 1) + "-" + year1;
                etDate.setText(date);
            }, year, month, day);
            datePickerDialog.show();
        });

        btnProceed.setOnClickListener(v -> {
            String date = etDate.getText().toString();
            String type = spinnerType.getSelectedItem().toString();

            if (date.isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
            } else if (type.equals("Select Type")) {
                Toast.makeText(this, "Please select attendance type", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MarkAttendanceActivity.this, TakeAttendanceActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });
    }
}