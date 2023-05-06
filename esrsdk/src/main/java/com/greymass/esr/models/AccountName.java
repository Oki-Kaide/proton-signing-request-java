package com.greymass.esr.models;

public class AccountName {
    private String gName;

    public AccountName(String name) {
        gName = name;
    }

    public void setName(String name) {
        gName = name;
    }

    public String getName() {
        return gName;
    }

    @Override
    public String toString() {
        return gName;
    }
}
