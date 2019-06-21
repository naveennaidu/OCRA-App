package com.naveennaidu.opc;

import java.util.ArrayList;

public class PatientModel {
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String patientPhone;
    private ArrayList<String> thumbnail;

    public PatientModel(){

    }

    public PatientModel(String name, String age, String gender, String phone, ArrayList<String> thumbnail){
        this.patientName = name;
        this.patientAge = age;
        this.patientGender = gender;
        this.patientPhone = phone;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return patientName;
    }

    public void setName(String name) {
        this.patientName = name;
    }

    public String getAge() {
        return patientAge;
    }

    public void setAge(String age) {
        this.patientAge = age;
    }

    public String getGender() {
        return patientGender;
    }

    public void setGender(String gender) {
        this.patientGender = gender;
    }

    public String getPhone() {
        return patientPhone;
    }

    public void setPhone(String phone) {
        this.patientPhone = phone;
    }

    public ArrayList<String> getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ArrayList<String> thumbnail) {
        this.thumbnail = thumbnail;
    }

}
