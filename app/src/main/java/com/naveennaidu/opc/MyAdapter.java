package com.naveennaidu.opc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private List<PatientModel> patientModelList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView name, age, gender, phone;
        public ImageView thumbnail;

        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.patientName);
            age = v.findViewById(R.id.patientAge);
            gender = v.findViewById(R.id.patientGender);
            phone = v.findViewById(R.id.patientPhoneNo);
            thumbnail = v.findViewById(R.id.thumbnail);
        }
    }

    public MyAdapter(Context context, List<PatientModel> patientModelList) {
        this.mContext = context;
        this.patientModelList = patientModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        PatientModel patient = patientModelList.get(i);
        myViewHolder.name.setText(patient.getName());
        myViewHolder.age.setText(patient.getAge());
        myViewHolder.gender.setText(patient.getGender());
        myViewHolder.phone.setText(patient.getPhone());

        // loading album cover using Glide library
        Glide.with(mContext).load(patient.getThumbnail().get(0)).into(myViewHolder.thumbnail);

        myViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("thumnail", "onClick: ");
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientModelList.size();
    }
}
