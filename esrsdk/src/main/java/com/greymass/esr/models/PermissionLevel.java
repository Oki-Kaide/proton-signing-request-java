package com.greymass.esr.models;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.greymass.esr.util.JSONUtil;

import java.util.Map;

public class PermissionLevel {
    public static String ACTOR = "actor";
    public static String PERMISSION = "permission";
    private AccountName gAccountName;
    private PermissionName gPermissionName;

    public PermissionLevel() {

    }

    public PermissionLevel(AccountName accountName, PermissionName permissionName) {
        gAccountName = accountName;
        gPermissionName = permissionName;
    }

    public PermissionLevel(String accountName, String permissionName) {
        gAccountName = new AccountName(accountName);
        gPermissionName = new PermissionName(permissionName);
    }

    public PermissionLevel(JsonObject obj) {
        if (obj.has(PERMISSION)) {
            if (obj.get(PERMISSION).isJsonObject())
                obj = obj.getAsJsonObject(PERMISSION);
            else if (obj.get(PERMISSION).isJsonNull())
                return;
        }

        gAccountName = new AccountName(obj.get(ACTOR).getAsString());
        gPermissionName = new PermissionName(obj.get(PERMISSION).getAsString());
    }

    public Map<String, String> toMap() {
        if (gAccountName == null || gPermissionName == null)
            return null;

        Map<String, String> map = Maps.newHashMap();
        map.put(ACTOR, gAccountName == null ? null : gAccountName.getName());
        map.put(PERMISSION, gPermissionName == null ? null : gPermissionName.getName());
        return map;
    }

    public void setAccountName(AccountName accountName) {
        gAccountName = accountName;
    }

    public AccountName getAccountName() {
        return gAccountName;
    }

    public void setPermissionName(PermissionName permissionName) {
        gPermissionName = permissionName;
    }

    public PermissionName getPermissionName() {
        return gPermissionName;
    }

    public String toJSON() {
        return JSONUtil.stringify(toMap());
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
