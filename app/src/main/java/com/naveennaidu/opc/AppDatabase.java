package com.naveennaidu.opc;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Patient.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PatientDao patientDao();
}
