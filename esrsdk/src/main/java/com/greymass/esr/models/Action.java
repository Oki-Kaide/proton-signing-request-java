package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.greymass.esr.ESRException;
import com.greymass.esr.interfaces.IRequest;
import com.greymass.esr.util.JSONUtil;

import java.util.List;
import java.util.Map;

import static com.greymass.esr.models.Identity.IDENTITY;

public class Action implements IRequest {
    public static final String VARIANT_TYPE = "action";

    private final String ACCOUNT = "account";
    private final String NAME = "name";
    private final String AUTHORIZATION = "authorization";
    private final String DATA = "data";
    private AccountName gAccount;
    private ActionName gName;
    private List<PermissionLevel> gAuthorization = Lists.newArrayList();
    private ActionData gData;

    public Action() {

    }

    public Action(JsonObject obj) throws ESRException {
        gAccount = new AccountName(obj.get(ACCOUNT).getAsString());
        gName = new ActionName(obj.get(NAME).getAsString());
        gAuthorization = getPermissionsFromJsonArray(obj.getAsJsonArray(AUTHORIZATION));
        if (obj.get(DATA).isJsonObject())
            gData = new ActionData(obj.getAsJsonObject(DATA));
        else
            gData = new ActionData(obj.get(DATA).getAsString());
    }

    private List<PermissionLevel> getPermissionsFromJsonArray(JsonArray array) throws ESRException {
        List<PermissionLevel> permissionLevels = Lists.newArrayList();
        for (JsonElement el : array) {
            if (!(el instanceof JsonObject))
                throw new ESRException("Permission was not an object");

            permissionLevels.add(new PermissionLevel((JsonObject) el));
        }
        return permissionLevels;
    }

    public boolean isIdentity() {
        return gAccount != null && "".equals(gAccount.getName()) &&
                gName != null && IDENTITY.equals(gName.getName());
    }

    public AccountName getAccount() {
        return gAccount;
    }

    public void setAccount(AccountName account) {
        gAccount = account;
    }

    public ActionName getName() {
        return gName;
    }

    public void setName(ActionName name) {
        gName = name;
    }

    public List<PermissionLevel> getAuthorization() {
        return gAuthorization;
    }

    public String getAuthorizationJSON() {
        List<Map<String, String>> toEncode = Lists.newArrayList();
        for (PermissionLevel level : gAuthorization)
            toEncode.add(level.toMap());

        return JSONUtil.stringify(toEncode);
    }

    public void addAuthorization(PermissionLevel authorization) {
        gAuthorization.add(authorization);
    }

    public ActionData getData() {
        return gData;
    }

    public void setData(ActionData data) {
        gData = data;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = Maps.newHashMap();
        result.put(ACCOUNT, gAccount.getName());
        result.put(NAME, gName.getName());
        List<Map<String, String>> auths = Lists.newArrayList();
        for (PermissionLevel permissionLevel : gAuthorization)
            auths.add(permissionLevel.toMap());

        result.put(AUTHORIZATION, auths);
        result.put(DATA, gData.isPacked() ? gData.getPackedData() : gData.getData());
        return result;
    }

    @Override
    public List<Action> getRawActions() {
        return Lists.newArrayList(this);
    }

    @Override
    public List<Object> toVariant() {
        List<Object> variant = Lists.newArrayList();
        variant.add(VARIANT_TYPE);
        variant.add(toMap());
        return variant;
    }

}
