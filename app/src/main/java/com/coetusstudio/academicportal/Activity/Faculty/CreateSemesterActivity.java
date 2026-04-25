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

public class CreateSemesterActivity extends AppCompatActivity {

    private EditText etSemesterName;
    private Button btnAddSemester;
    private RecyclerView rvRecentSemesters;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<String, SemesterViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_semester);

        Toolbar toolbar = findViewById(R.id.toolbarCreateSemester);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etSemesterName = findViewById(R.id.etSemesterName);
        btnAddSemester = findViewById(R.id.btnAddSemester);
        rvRecentSemesters = findViewById(R.id.rvRecentSemesters);
        rvRecentSemesters.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Semesters");

        btnAddSemester.setOnClickListener(v -> {
            String semesterName = etSemesterName.getText().toString().trim();
            if (semesterName.isEmpty()) {
                etSemesterName.setError("Required");
            } else {
                reference.child(semesterName).setValue(semesterName).addOnSuccessListener(unused -> {
                    etSemesterName.setText("");
                    Toast.makeText(this, "Semester Added Successfully", Toast.LENGTH_SHORT).show();
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

        adapter = new FirebaseRecyclerAdapter<String, SemesterViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SemesterViewHolder holder, int position, @NonNull String model) {
                holder.tvSemesterName.setText(model);
                holder.ivDeleteSemester.setOnClickListener(v -> {
                    new AlertDialog.Builder(CreateSemesterActivity.this)
                            .setTitle("Delete Semester")
                            .setMessage("Are you sure you want to delete this semester?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getRef(position).removeValue();
                                Toast.makeText(CreateSemesterActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            @NonNull
            @Override
            public SemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester, parent, false);
                return new SemesterViewHolder(view);
            }
        };

        rvRecentSemesters.setAdapter(adapter);
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

    public static class SemesterViewHolder extends RecyclerView.ViewHolder {
        TextView tvSemesterName;
        ImageView ivDeleteSemester;

        public SemesterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSemesterName = itemView.findViewById(R.id.tvSemesterName);
            ivDeleteSemester = itemView.findViewById(R.id.ivDeleteSemester);
        }
    }
}
