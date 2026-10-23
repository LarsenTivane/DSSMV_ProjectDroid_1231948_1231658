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

    public StudyGroup(String title, String subject, int vacancies) {
        this.title = title;
        this.subject = subject;
        this.vacancies = vacancies;
    }

    public String getId() { return id; } // <-- Adicionado para o DELETE funcionar
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public int getVacancies() { return vacancies; }
}