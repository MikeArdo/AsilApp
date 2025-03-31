package it.bugbuster.asilapp.entity;

import java.util.List;

public class RefugeeShelter {
    private String id;
    private String name;
    private String city;
    private Description description;
    private Rules rules;
    private Services services;
    private String image;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public static class Description {
        private String it;
        private String en;

        // Getters and Setters
        public String getIt() {
            return it;
        }

        public void setIt(String it) {
            this.it = it;
        }

        public String getEn() {
            return en;
        }

        public void setEn(String en) {
            this.en = en;
        }
    }

    public static class Rules {
        private List<String> it;
        private List<String> en;

        // Getters and Setters
        public List<String> getIt() {
            return it;
        }

        public void setIt(List<String> it) {
            this.it = it;
        }

        public List<String> getEn() {
            return en;
        }

        public void setEn(List<String> en) {
            this.en = en;
        }
    }

    public static class Services {
        private List<String> it;
        private List<String> en;

        // Getters and Setters
        public List<String> getIt() {
            return it;
        }

        public void setIt(List<String> it) {
            this.it = it;
        }

        public List<String> getEn() {
            return en;
        }

        public void setEn(List<String> en) {
            this.en = en;
        }
    }
}
