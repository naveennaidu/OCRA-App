package com.naveennaidu.opc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class PatientDatabaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<PatientModel> patientModelList;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_patient);
        setTitle("Patient Database");
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        patientModelList = new ArrayList<>();
        adapter = new MyAdapter(this, patientModelList);
        recyclerView.setAdapter(adapter);
        downloadData();
    }

    private void downloadData(){

        PatientModel p1 = new PatientModel("Naveen", "21", "Male", "0000000000", R.drawable.ic_launcher_background);
        patientModelList.add(p1);
        adapter.notifyDataSetChanged();
        Log.e("tag", "downloadData: Yes");
    }
}
