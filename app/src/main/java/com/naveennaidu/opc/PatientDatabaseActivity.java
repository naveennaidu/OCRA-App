package com.naveennaidu.opc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;


public class PatientDatabaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<PatientModel> patientModelList;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseFirestore db;
    List<PatientModel> patientModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_patient);
        setTitle("Patient Database");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        patientModelList = new ArrayList<>();
        adapter = new MyAdapter(this, patientModelList);
        recyclerView.setAdapter(adapter);
        downloadData();
    }

    private void downloadData(){
        db = FirebaseFirestore.getInstance();

        // Source can be CACHE, SERVER, or DEFAULT.
        Source source = Source.CACHE;

        db.collection("patientstest")
                .get(source)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        patientModel = queryDocumentSnapshots.toObjects(PatientModel.class);
                        patientModelList.addAll(patientModel);

//                        PatientModel p1 = new PatientModel(patientModel.get(0).getName(), patientModel.get(0).getAge(), patientModel.get(0).getGender(), patientModel.get(0).getPhone(), patientModel.get(0).getThumbnail());
//                        patientModelList.add(p1);

                        adapter.notifyDataSetChanged();

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close_button) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
