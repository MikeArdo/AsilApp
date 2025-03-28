package it.bugbuster.asilapp.entity;

public class Doctor extends User {
    private String licenseNumber;


    public Doctor(String id, String name, String surname, String email, String birthDate, String licenseNumber) {
        super(id, name, surname, email, birthDate);
        this.licenseNumber = licenseNumber;
    }

    // Getter e Setter
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}