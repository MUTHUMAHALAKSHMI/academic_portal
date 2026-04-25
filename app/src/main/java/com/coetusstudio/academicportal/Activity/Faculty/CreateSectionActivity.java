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

public class CreateSectionActivity extends AppCompatActivity {

    private EditText etSectionName;
    private Button btnAddSection;
    private RecyclerView rvRecentSections;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<String, SectionViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_section);

        Toolbar toolbar = findViewById(R.id.toolbarCreateSection);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etSectionName = findViewById(R.id.etSectionName);
        btnAddSection = findViewById(R.id.btnAddSection);
        rvRecentSections = findViewById(R.id.rvRecentSections);
        rvRecentSections.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Admin Data").child("Sections");

        btnAddSection.setOnClickListener(v -> {
            String sectionName = etSectionName.getText().toString().trim().toUpperCase();
            if (sectionName.isEmpty()) {
                etSectionName.setError("Required");
            } else {
                reference.child(sectionName).setValue(sectionName).addOnSuccessListener(unused -> {
                    etSectionName.setText("");
                    Toast.makeText(this, "Section Added Successfully", Toast.LENGTH_SHORT).show();
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

        adapter = new FirebaseRecyclerAdapter<String, SectionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SectionViewHolder holder, int position, @NonNull String model) {
                holder.tvSectionName.setText(model);
                holder.ivDeleteSection.setOnClickListener(v -> {
                    new AlertDialog.Builder(CreateSectionActivity.this)
                            .setTitle("Delete Section")
                            .setMessage("Are you sure you want to delete this section?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                getRef(position).removeValue();
                                Toast.makeText(CreateSectionActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            @NonNull
            @Override
            public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
                return new SectionViewHolder(view);
            }
        };

        rvRecentSections.setAdapter(adapter);
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

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionName;
        ImageView ivDeleteSection;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
            ivDeleteSection = itemView.findViewById(R.id.ivDeleteSection);
        }
    }
}
