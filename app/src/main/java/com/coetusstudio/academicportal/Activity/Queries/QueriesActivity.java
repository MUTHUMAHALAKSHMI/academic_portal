package com.coetusstudio.academicportal.Activity.Queries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.coetusstudio.academicportal.Adapter.QueriesAdapter;
import com.coetusstudio.academicportal.Model.Queries;
import com.coetusstudio.academicportal.R;
import com.coetusstudio.academicportal.databinding.ActivityQueriesBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QueriesActivity extends AppCompatActivity {

    ActivityQueriesBinding binding;
    DatabaseReference reference, dbfacultyref, dbhodref, studentDataRef;
    String studentName, studentRollNumber, facultyName, studentImage, studentSection;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    QueriesAdapter queriesAdapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQueriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        
        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Queries");
        dbfacultyref = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        dbhodref = FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data");
        studentDataRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Queries");
        progressDialog.setMessage("Sending...");

        // 1. Initialize Spinner
        final List<String> listFacultyName = new ArrayList<>();
        listFacultyName.add("Select Faculty/HOD Name");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listFacultyName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.queriesFaculty.setAdapter(adapter);

        binding.queriesFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                facultyName = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 2. Fetch All Faculty
        dbfacultyref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    for (DataSnapshot faculty : dept.getChildren()) {
                        String name = String.valueOf(faculty.child("facultyName").getValue());
                        if (name != null && !name.equals("null") && !listFacultyName.contains(name)) {
                            listFacultyName.add(name);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. Fetch All HODs
        dbhodref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    for (DataSnapshot hod : dept.getChildren()) {
                        String name = String.valueOf(hod.child("hodName").getValue());
                        if (name != null && !name.equals("null")) {
                            String hodEntry = name + " (HOD)";
                            if (!listFacultyName.contains(hodEntry)) {
                                listFacultyName.add(hodEntry);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 4. Fetch Student Info & Setup Recycler
        if (currentUser != null) {
            String uid = currentUser.getUid();
            studentDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dept : snapshot.getChildren()) {
                        if (dept.hasChild(uid)) {
                            studentSection = dept.getKey();
                            DataSnapshot user = dept.child(uid);
                            studentName = String.valueOf(user.child("studentName").getValue());
                            studentRollNumber = String.valueOf(user.child("studentRollNumber").getValue());
                            studentImage = String.valueOf(user.child("studentImage").getValue());
                            
                            setupRecyclerView();
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        binding.btnSendLink.setOnClickListener(v -> {
            String title = binding.queriesTitle.getEditText().getText().toString();
            if (title.isEmpty()) {
                binding.queriesTitle.setError("Required");
            } else if (facultyName == null || facultyName.equals("Select Faculty/HOD Name")) {
                Toast.makeText(this, "Select a recipient", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
                sendQuery(title, progressDialog);
            }
        });
    }

    private void setupRecyclerView() {
        if (studentSection == null || studentRollNumber == null) return;
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.rcQueries.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<Queries> options = new FirebaseRecyclerOptions.Builder<Queries>()
                .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Resolve Queries").child(studentSection).child(studentRollNumber), Queries.class)
                .build();
        
        queriesAdapter = new QueriesAdapter(options);
        binding.rcQueries.setAdapter(queriesAdapter);
        queriesAdapter.startListening();
    }

    private void sendQuery(String title, ProgressDialog pd) {
        String cleanName = facultyName.replace(" (HOD)", "");
        Queries query = new Queries(studentName, studentRollNumber, cleanName, title, studentImage);
        
        reference.child(studentSection).child(cleanName).push().setValue(query).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(this, "Query Sent", Toast.LENGTH_SHORT).show();
            binding.queriesTitle.getEditText().setText("");
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queriesAdapter != null) queriesAdapter.stopListening();
    }
}
