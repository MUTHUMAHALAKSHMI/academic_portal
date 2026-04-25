package com.coetusstudio.academicportal.Activity.Faculty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateSubjectActivity extends AppCompatActivity {

    private EditText etSubjectName;
    private Button btnAddSubject;
    private RecyclerView rvRecentSubjects;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<String, SubjectViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subject);

        Toolbar toolbar = findViewById(R.id.toolbarCreateSubject);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etSubjectName = findViewById(R.id.etSubjectName);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        rvRecentSubjects = findViewById(R.id.rvRecentSubjects);
        rvRecentSubjects.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Subjects");

        btnAddSubject.setOnClickListener(v -> {
            String subjectName = etSubjectName.getText().toString().trim();
            if (subjectName.isEmpty()) {
                etSubjectName.setError("Required");
            } else {
                reference.child(subjectName).setValue(subjectName).addOnSuccessListener(unused -> {
                    etSubjectName.setText("");
                    Toast.makeText(this, "Subject Added Successfully", Toast.LENGTH_SHORT).show();
                });
            }
        });

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(reference, String.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<String, SubjectViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SubjectViewHolder holder, int position, @NonNull String model) {
                holder.tvSubjectName.setText(model);
                holder.ivDeleteSubject.setOnClickListener(v -> {
                    new AlertDialog.Builder(CreateSubjectActivity.this)
                            .setTitle("Delete Subject")
                            .setMessage("Are you sure you want to delete this subject?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getRef(position).removeValue();
                                Toast.makeText(CreateSubjectActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            @NonNull
            @Override
            public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
                return new SubjectViewHolder(view);
            }
        };

        rvRecentSubjects.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName;
        ImageView ivDeleteSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            ivDeleteSubject = itemView.findViewById(R.id.ivDeleteSubject);
        }
    }
}
