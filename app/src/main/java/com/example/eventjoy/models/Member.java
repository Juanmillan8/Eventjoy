package com.example.eventjoy.models;

import com.example.eventjoy.enums.Provider;

import java.time.LocalDate;

public class Member extends Person{

    private String dni;
    private String phone;
    private String birthdate;
    private String username;
    private Integer level;
    private Provider provider;

    public Member() {
        super();
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Member{" +
                "dni='" + dni + '\'' +
                ", phone='" + phone + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", username='" + username + '\'' +
                ", level=" + level +
                ", provider=" + provider +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", photo='" + photo + '\'' +
                ", role=" + role +
                ", userAccountId='" + userAccountId + '\'' +
                '}';
    }
}