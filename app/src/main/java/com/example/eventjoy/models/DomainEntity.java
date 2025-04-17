package com.example.eventjoy.models;

import java.io.Serializable;

public class DomainEntity implements Serializable {

    private String id;

    public DomainEntity() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DomainEntity{" +
                "id='" + id + '\'' +
                '}';
    }

}
