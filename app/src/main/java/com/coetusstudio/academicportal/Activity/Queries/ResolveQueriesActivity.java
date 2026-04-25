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
import com.coetusstudio.academicportal.databinding.ActivityResolveQueriesBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResolveQueriesActivity extends AppCompatActivity {

    ActivityResolveQueriesBinding binding;
    DatabaseReference reference, studentRef, resolveRef;
    String facultyName, facultyDept, facultyImage, studentRollNumber;
    FirebaseAuth auth;
    QueriesAdapter queriesAdapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResolveQueriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Queries");
        studentRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data");
        resolveRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Resolve Queries");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Resolving Query...");

        getFacultyOrHODDetails();

        binding.btnResolveSend.setOnClickListener(v -> {
            if (binding.resolveQueriesTitle.getEditText() != null && binding.resolveQueriesTitle.getEditText().getText().toString().isEmpty()) {
                binding.resolveQueriesTitle.setError("Required");
            } else if (studentRollNumber == null || studentRollNumber.equals("Select Roll Number")) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
                resolveQuery();
                progressDialog.dismiss();
            }
        });
    }

    private void getFacultyOrHODDetails() {
        String uid = auth.getUid();
        if (uid == null) return;

        // Check Faculty Data
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        facultyDept = dept.getKey();
                        DataSnapshot userSnap = dept.child(uid);
                        
                        // Safe conversion to String
                        facultyName = String.valueOf(userSnap.child("facultyName").getValue());
                        facultyImage = String.valueOf(userSnap.child("facultyImage").getValue());
                        
                        loadStudents();
                        setupRecyclerView();
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // Check Hod Data
                    FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dept : snapshot.getChildren()) {
                                if (dept.hasChild(uid)) {
                                    facultyDept = dept.getKey();
                                    DataSnapshot userSnap = dept.child(uid);
                                    
                                    // Safe conversion to String
                                    facultyName = String.valueOf(userSnap.child("hodName").getValue());
                                    facultyImage = String.valueOf(userSnap.child("hodImage").getValue());
                                    
                                    loadStudents();
                                    setupRecyclerView();
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStudents() {
        List<String> rollNumbers = new ArrayList<>();
        rollNumbers.add("Select Roll Number");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rollNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.queriesRollNumber.setAdapter(adapter);

        if (facultyDept != null) {
            studentRef.child(facultyDept).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot student : snapshot.getChildren()) {
                        String roll = String.valueOf(student.child("studentRollNumber").getValue());
                        if (roll != null && !roll.equals("null")) {
                            rollNumbers.add(roll);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        binding.queriesRollNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                studentRollNumber = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        if (facultyDept == null || facultyName == null || facultyName.equals("null")) return;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.rcStudentQueries.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<Queries> options = new FirebaseRecyclerOptions.Builder<Queries>()
                .setQuery(reference.child(facultyDept).child(facultyName), Queries.class)
                .build();

        queriesAdapter = new QueriesAdapter(options);
        binding.rcStudentQueries.setAdapter(queriesAdapter);
        queriesAdapter.startListening();
    }

    private void resolveQuery() {
        if (binding.resolveQueriesTitle.getEditText() == null) return;
        String resolvedTitle = binding.resolveQueriesTitle.getEditText().getText().toString();
        Queries queries = new Queries(facultyName, studentRollNumber, facultyName, resolvedTitle, facultyImage, "");

        resolveRef.child(facultyDept).child(studentRollNumber).push().setValue(queries).addOnSuccessListener(unused -> {
            Toast.makeText(ResolveQueriesActivity.this, "Query Resolved", Toast.LENGTH_SHORT).show();
            binding.resolveQueriesTitle.getEditText().setText("");
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queriesAdapter != null) queriesAdapter.stopListening();
    }
}
