package com.naveennaidu.opc;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM patient")
    List<Patient> getAll();

    @Query("SELECT * FROM patient WHERE uid IN (:patientIds)")
    List<Patient> loadAllByIds(Integer[] patientIds);

    @Query("SELECT * FROM patient WHERE name LIKE :name LIMIT 1")
    Patient findByName(String name);

    @Query("SELECT * FROM patient where name LIKE  :name AND age LIKE :age")
    Patient findByNameAge(String name, int age);

    @Insert
    void insert(Patient products);

    @Update
    void update(Patient product);

    @Delete
    void delete(Patient product);
}
