package com.example.eventjoy.models;

import com.example.eventjoy.enums.Visibility;

public class Group extends DomainEntity{

    private String title;
    private String description;
    private Visibility visibility;
    private String icon;

    public Group() {
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Group{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", visibility=" + visibility +
                ", icon='" + icon + '\'' +
                '}';
    }
}
