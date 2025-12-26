package com.aura.clinician.domain;

public class CaseSummary {
    private String caseId;
    private int age;
    private String gender;

    public CaseSummary() {
    }

    public CaseSummary(String caseId, int age, String gender) {
        this.caseId = caseId;
        this.age = age;
        this.gender = gender;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
