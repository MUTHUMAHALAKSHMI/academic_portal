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

public class CreateSessionalTitleActivity extends AppCompatActivity {

    private EditText etSessionalTitle;
    private Button btnAddSessionalTitle;
    private RecyclerView rvRecentSessionalTitles;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<String, SessionalTitleViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sessional_title);

        Toolbar toolbar = findViewById(R.id.toolbarCreateSessionalTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etSessionalTitle = findViewById(R.id.etSessionalTitle);
        btnAddSessionalTitle = findViewById(R.id.btnAddSessionalTitle);
        rvRecentSessionalTitles = findViewById(R.id.rvRecentSessionalTitles);
        rvRecentSessionalTitles.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Sessional Titles");

        btnAddSessionalTitle.setOnClickListener(v -> {
            String title = etSessionalTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etSessionalTitle.setError("Required");
            } else {
                reference.child(title).setValue(title).addOnSuccessListener(unused -> {
                    etSessionalTitle.setText("");
                    Toast.makeText(this, "Title Added Successfully", Toast.LENGTH_SHORT).show();
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

        adapter = new FirebaseRecyclerAdapter<String, SessionalTitleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SessionalTitleViewHolder holder, int position, @NonNull String model) {
                holder.tvSessionalTitle.setText(model);
                holder.ivDeleteSessionalTitle.setOnClickListener(v -> {
                    new AlertDialog.Builder(CreateSessionalTitleActivity.this)
                            .setTitle("Delete Title")
                            .setMessage("Are you sure you want to delete this title?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getRef(position).removeValue();
                                Toast.makeText(CreateSessionalTitleActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            @NonNull
            @Override
            public SessionalTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sessional_title, parent, false);
                return new SessionalTitleViewHolder(view);
            }
        };

        rvRecentSessionalTitles.setAdapter(adapter);
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

    public static class SessionalTitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionalTitle;
        ImageView ivDeleteSessionalTitle;

        public SessionalTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionalTitle = itemView.findViewById(R.id.tvSessionalTitle);
            ivDeleteSessionalTitle = itemView.findViewById(R.id.ivDeleteSessionalTitle);
        }
    }
}
