package it.bugbuster.asilapp.entity;

public class AsylumSeeker extends User {
    private String refugeeShelter;


    public AsylumSeeker(String id, String name, String surname, String email, String birthDate, String refugeeShelter) {
        super(id, name, surname, email, birthDate);
        this.refugeeShelter = refugeeShelter;
    }

    // Getter e Setter
    public String getRefugeeShelter() {
        return refugeeShelter;
    }

    public void setRefugeeShelter(String refugeeShelter) {
        this.refugeeShelter = refugeeShelter;
    }
}