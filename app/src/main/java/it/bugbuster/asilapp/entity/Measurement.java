package it.bugbuster.asilapp.entity;

public class Measurement {

    private String type;
    private String date;
    private String value;

    public Measurement(String type, String date, String value) {
        this.value = value;
        this.type = type;
        this.date = date;
    }

    // Getters and setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
