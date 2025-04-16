package com.example.eventjoy.models;

public class Valoration extends DomainEntity{

    private String title;
    private String description;
    private Double rating;
    private String ratedUserId;
    private String raterUserId;

    public Valoration() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getRatedUserId() {
        return ratedUserId;
    }

    public void setRatedUserId(String ratedUserId) {
        this.ratedUserId = ratedUserId;
    }

    public String getRaterUserId() {
        return raterUserId;
    }

    public void setRaterUserId(String raterUserId) {
        this.raterUserId = raterUserId;
    }

    @Override
    public String toString() {
        return "Valoration{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                ", ratedUserId='" + ratedUserId + '\'' +
                ", raterUserId='" + raterUserId + '\'' +
                '}';
    }
}
