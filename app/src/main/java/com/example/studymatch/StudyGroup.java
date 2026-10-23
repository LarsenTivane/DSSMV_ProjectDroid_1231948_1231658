package com.example.studymatch;

import com.google.gson.annotations.SerializedName;

public class StudyGroup {
    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("subject")
    private String subject;

    @SerializedName("vacancies")
    private int vacancies;

    @SerializedName("turma")
    private String turma;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("distanciaMetros")
    private transient float distanciaMetros;

    public StudyGroup() {
    }

    public StudyGroup(String title, String subject, int vacancies) {
        this.title = title;
        this.subject = subject;
        this.vacancies = vacancies;
    }

    public StudyGroup(String title, String subject, int vacancies, String turma, double latitude, double longitude) {
        this.title = title;
        this.subject = subject;
        this.vacancies = vacancies;
        this.turma = turma;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public int getVacancies() { return vacancies; }
    public void setVacancies(int vacancies) { this.vacancies = vacancies; }

    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getDistanciaMetros() { return distanciaMetros; }
    public void setDistanciaMetros(float distanciaMetros) { this.distanciaMetros = distanciaMetros; }
}