package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Model.HODData;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class HodManagementActivity extends AppCompatActivity {

    private RecyclerView rvHODList;
    private FloatingActionButton fabAddHOD;
    private SearchView searchViewHOD;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<HODData, HODViewHolder> adapter;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_management);

        Toolbar toolbar = findViewById(R.id.toolbarHODManagement);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("HOD Data");

        rvHODList = findViewById(R.id.rvHODList);
        rvHODList.setLayoutManager(new LinearLayoutManager(this));

        fabAddHOD = findViewById(R.id.fabAddHOD);
        searchViewHOD = findViewById(R.id.searchViewHOD);

        fabAddHOD.setOnClickListener(v -> startActivity(new Intent(this, AddHODActivity.class)));

        searchViewHOD.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadHODList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadHODList(newText);
                return false;
            }
        });

        loadHODList("");
    }

    private void loadHODList(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            query = reference;
        } else {
            // Note: This search is case-sensitive and works by branch or name depending on how your DB is structured.
            // Usually, searching by name requires indexing.
            query = reference.orderByChild("hodName").startAt(searchText).endAt(searchText + "\uf8ff");
        }

        FirebaseRecyclerOptions<HODData> options = new FirebaseRecyclerOptions.Builder<HODData>()
                .setQuery(query, HODData.class)
                .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<HODData, HODViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HODViewHolder holder, int position, @NonNull HODData model) {
                holder.name.setText(model.getHodName());
                holder.branch.setText(model.getHodBranch());
                holder.email.setText(model.getHodEmail());

                if (model.getHodImage() != null && !model.getHodImage().isEmpty()) {
                    Glide.with(HodManagementActivity.this)
                            .load(model.getHodImage())
                            .placeholder(R.drawable.manimg)
                            .into(holder.image);
                } else {
                    holder.image.setImageResource(R.drawable.manimg);
                }

                holder.btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(HodManagementActivity.this)
                            .setTitle("Delete HOD")
                            .setMessage("Are you sure you want to delete " + model.getHodName() + "?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                reference.child(model.getHodBranch()).child(model.getHodName()).removeValue()
                                        .addOnSuccessListener(unused -> Toast.makeText(HodManagementActivity.this, "HOD Deleted", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(HodManagementActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

                holder.btnEdit.setOnClickListener(v -> {
                    // Navigate to AddHODActivity with data for updating
                    Intent intent = new Intent(HodManagementActivity.this, AddHODActivity.class);
                    intent.putExtra("isUpdate", true);
                    intent.putExtra("name", model.getHodName());
                    intent.putExtra("email", model.getHodEmail());
                    intent.putExtra("branch", model.getHodBranch());
                    intent.putExtra("phone", model.getHodPhone());
                    intent.putExtra("id", model.getHodId());
                    intent.putExtra("image", model.getHodImage());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public HODViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hod, parent, false);
                return new HODViewHolder(view);
            }
        };

        rvHODList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class HODViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, branch, email;
        ImageView btnEdit, btnDelete;

        public HODViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.hodItemImage);
            name = itemView.findViewById(R.id.hodItemName);
            branch = itemView.findViewById(R.id.hodItemBranch);
            email = itemView.findViewById(R.id.hodItemEmail);
            btnEdit = itemView.findViewById(R.id.btnEditHOD);
            btnDelete = itemView.findViewById(R.id.btnDeleteHOD);
        }
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
}
