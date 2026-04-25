package com.coetusstudio.academicportal.Activity.Attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DayWiseAttendanceActivity extends AppCompatActivity {

    private String date, dept, subject;
    private TextView tvSubject, tvSection, tvDate, tvTotal;
    private RecyclerView recyclerView;
    private List<AttendanceModel> attendanceList = new ArrayList<>();
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_wise_attendance);

        date = getIntent().getStringExtra("date");
        dept = getIntent().getStringExtra("dept");
        subject = getIntent().getStringExtra("subject");

        if (date == null || dept == null || subject == null) {
            Toast.makeText(this, "Missing data to display attendance", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvSubject = findViewById(R.id.tvSubjectName);
        tvSection = findViewById(R.id.tvSectionName);
        tvDate = findViewById(R.id.tvDate);
        tvTotal = findViewById(R.id.tvTotalStudents);
        recyclerView = findViewById(R.id.rvDayAttendance);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvSubject.setText("Subject Name: " + subject);
        tvSection.setText("Section: " + dept);
        tvDate.setText("Date: " + date);

        fetchAttendance();
    }

    private void fetchAttendance() {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(dept).child(subject).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                
                attendanceList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String roll = ds.getKey();
                    Object valObj = ds.child("atvalue").getValue();
                    String status = valObj != null ? String.valueOf(valObj) : "N/A";
                    attendanceList.add(new AttendanceModel(roll, status));
                }
                recyclerView.setAdapter(new DayAdapter(attendanceList));
                tvTotal.setText("Total Number Of Student= " + attendanceList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private static class AttendanceModel {
        String roll, status;
        AttendanceModel(String roll, String status) {
            this.roll = roll;
            this.status = status;
        }
    }

    private class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        private List<AttendanceModel> list;
        DayAdapter(List<AttendanceModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_day_attendance, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.roll.setText(list.get(position).roll);
            holder.status.setText(list.get(position).status);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView roll, status;
            ViewHolder(View v) {
                super(v);
                roll = v.findViewById(R.id.tvDayRoll);
                status = v.findViewById(R.id.tvDayStatus);
            }
        }
    }
}