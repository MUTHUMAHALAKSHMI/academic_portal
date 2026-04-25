package com.coetusstudio.academicportal.Activity.Attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.Model.StudentDetails;
import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectiveAttendanceActivity extends AppCompatActivity {

    private String dept, subject;
    private TextView tvSubject;
    private RecyclerView recyclerView;
    private List<CollectiveModel> collectiveList = new ArrayList<>();
    private Map<String, String> studentNames = new HashMap<>();
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collective_attendance);

        dept = getIntent().getStringExtra("dept");
        subject = getIntent().getStringExtra("subject");

        tvSubject = findViewById(R.id.tvCollSubject);
        recyclerView = findViewById(R.id.rvCollectiveAttendance);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvSubject.setText(subject);

        fetchStudentNames();
    }

    private void fetchStudentNames() {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").child(dept).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    studentNames.put(ds.getKey(), ds.child("studentName").getValue(String.class));
                }
                fetchCollectiveAttendance();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCollectiveAttendance() {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet")
                .child(dept).child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, int[]> stats = new HashMap<>(); // roll -> [present, total]
                
                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    for (DataSnapshot rollSnap : dateSnap.getChildren()) {
                        String roll = rollSnap.getKey();
                        String value = rollSnap.child("atvalue").getValue(String.class);
                        if (value != null && value.length() >= 3) {
                            int present = Integer.parseInt(value.substring(0, 1));
                            int total = Integer.parseInt(value.substring(2, 3));
                            
                            int[] current = stats.getOrDefault(roll, new int[]{0, 0});
                            current[0] += present;
                            current[1] += total;
                            stats.put(roll, current);
                        }
                    }
                }
                
                collectiveList.clear();
                for (Map.Entry<String, int[]> entry : stats.entrySet()) {
                    String roll = entry.getKey();
                    String name = studentNames.getOrDefault(roll, "Unknown");
                    collectiveList.add(new CollectiveModel(name, roll, entry.getValue()[0], entry.getValue()[1]));
                }
                recyclerView.setAdapter(new CollectiveAdapter(collectiveList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private static class CollectiveModel {
        String name, roll;
        int present, total;
        CollectiveModel(String name, String roll, int present, int total) {
            this.name = name; this.roll = roll; this.present = present; this.total = total;
        }
    }

    private class CollectiveAdapter extends RecyclerView.Adapter<CollectiveAdapter.ViewHolder> {
        private List<CollectiveModel> list;
        CollectiveAdapter(List<CollectiveModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_collective_attendance, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CollectiveModel m = list.get(position);
            holder.name.setText(m.name);
            holder.roll.setText(m.roll);
            holder.att.setText("Attendance: " + m.present + "/" + m.total);
            holder.abs.setText("Absent: " + (m.total - m.present));
            float perc = m.total > 0 ? ((float) m.present / m.total) * 100 : 0;
            holder.perc.setText(String.format("Percentage: %.1f %%", perc));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, roll, att, abs, perc;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.tvCollName);
                roll = v.findViewById(R.id.tvCollRoll);
                att = v.findViewById(R.id.tvCollAtt);
                abs = v.findViewById(R.id.tvCollAbs);
                perc = v.findViewById(R.id.tvCollPerc);
            }
        }
    }
}