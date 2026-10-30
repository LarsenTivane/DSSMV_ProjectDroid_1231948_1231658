package com.example.studymatch;

import com.google.gson.annotations.SerializedName;

public class StudyGroupMember {
    @SerializedName("memberId")
    private String memberId;

    @SerializedName("name")
    private String name;

    @SerializedName("studentNumber")
    private String studentNumber;

    public StudyGroupMember() {
    }

    public StudyGroupMember(String memberId, String name, String studentNumber) {
        this.memberId = memberId;
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}
