package it.bugbuster.asilapp.entity;

public class AsylumSeeker extends User {
    private RefugeeShelter refugeeShelter;


    public AsylumSeeker(String name, String surname, String email, String birthDate, RefugeeShelter refugeeShelter) {
        super(name, surname, email, birthDate);
        this.refugeeShelter = refugeeShelter;
    }

    // Getter e Setter
    public RefugeeShelter getRefugeeShelter() {
        return refugeeShelter;
    }

    public void setRefugeeShelter(RefugeeShelter refugeeShelter) {
        this.refugeeShelter = refugeeShelter;
    }
}