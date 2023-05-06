package com.greymass.esr.models;

public class PermissionName {
    private String gName;

    public PermissionName(String name) {
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
