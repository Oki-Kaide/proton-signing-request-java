package com.greymass.esr.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.greymass.esr.interfaces.IRequest;

import java.util.List;
import java.util.Map;

import static com.greymass.esr.models.PermissionLevel.PERMISSION;

public class Identity implements IRequest {
    public static final String IDENTITY = "identity";
    public static final String VARIANT_TYPE = IDENTITY;
    private PermissionLevel gPermissionLevel;

    public Identity() {
        gPermissionLevel = new PermissionLevel();
    }

    public Identity(PermissionLevel permissionLevel) {
        gPermissionLevel = permissionLevel;
    }

    public PermissionLevel getPermissionLevel() {
        return gPermissionLevel;
    }

    @Override
    public List<Action> getRawActions() {
        return null;
    }

    @Override
    public List<Object> toVariant() {
        List<Object> variant = Lists.newArrayList();
        variant.add(VARIANT_TYPE);
        variant.add(toMap());
        return variant;
    }

    private Map<String, Object> toMap() {
        Map<String, Object> result = Maps.newHashMap();
        Map<String, String> permissionLevel = gPermissionLevel.toMap();
        result.put(PERMISSION, permissionLevel);
        return result;
    }
}
