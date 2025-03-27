package it.bugbuster.asilapp.entity;

public class Disease {
    private String user_id;
    private String doctor_id;
    private String disease;
    private String therapy;
    public Disease(String user_id, String doctor_id, String disease, String therapy) {
        this.user_id = user_id;
        this.doctor_id = doctor_id;
        this.disease = disease;
        this.therapy = therapy;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getTherapy() {
        return therapy;
    }

    public void setTherapy(String therapy) {
        this.therapy = therapy;
    }
}
