package com.example.eventjoy.models;

import com.example.eventjoy.enums.Provider;

public class Admin extends Person{

    private Provider provider;

    public Admin() {
        super();
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "provider=" + provider +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", photo='" + photo + '\'' +
                ", role=" + role +
                ", userAccountId='" + userAccountId + '\'' +
                '}';
    }
}