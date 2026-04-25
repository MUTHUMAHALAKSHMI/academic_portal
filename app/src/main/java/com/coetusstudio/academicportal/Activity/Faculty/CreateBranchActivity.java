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

public class CreateBranchActivity extends AppCompatActivity {

    private EditText etBranchName;
    private Button btnAddBranch;
    private RecyclerView rvRecentBranches;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<String, BranchViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_branch);

        Toolbar toolbar = findViewById(R.id.toolbarCreateBranch);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etBranchName = findViewById(R.id.etBranchName);
        btnAddBranch = findViewById(R.id.btnAddBranch);
        rvRecentBranches = findViewById(R.id.rvRecentBranches);
        rvRecentBranches.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Branches");

        btnAddBranch.setOnClickListener(v -> {
            String branchName = etBranchName.getText().toString().trim().toUpperCase();
            if (branchName.isEmpty()) {
                etBranchName.setError("Required");
            } else {
                reference.child(branchName).setValue(branchName).addOnSuccessListener(unused -> {
                    etBranchName.setText("");
                    Toast.makeText(this, "Branch Added Successfully", Toast.LENGTH_SHORT).show();
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

        adapter = new FirebaseRecyclerAdapter<String, BranchViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BranchViewHolder holder, int position, @NonNull String model) {
                holder.tvBranchName.setText(model);
                holder.ivDeleteBranch.setOnClickListener(v -> {
                    new AlertDialog.Builder(CreateBranchActivity.this)
                            .setTitle("Delete Branch")
                            .setMessage("Are you sure you want to delete this branch?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getRef(position).removeValue();
                                Toast.makeText(CreateBranchActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            @NonNull
            @Override
            public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch, parent, false);
                return new BranchViewHolder(view);
            }
        };

        rvRecentBranches.setAdapter(adapter);
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

    public static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranchName;
        ImageView ivDeleteBranch;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            ivDeleteBranch = itemView.findViewById(R.id.ivDeleteBranch);
        }
    }
}
