package com.greymass.esr.models;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.greymass.esr.util.JSONUtil;

import java.util.Map;

public class Signature {

    private static final String SIGNER = "signer";
    private static final String SIGNATURE = "signature";
    private String gSigner;
    private String gSignature;

    public Signature(String signer, String signature) {
        gSigner = signer;
        gSignature = signature;
    }

    public Signature(JsonObject jsonObject) {
        gSigner = jsonObject.get(SIGNER).getAsString();
        gSignature = jsonObject.get(SIGNATURE).getAsString();
    }

    public String getSigner() {
        return gSigner;
    }

    public String getSignature() {
        return gSignature;
    }

    public String toJSON() {
        Map<String, String> toEncode = Maps.newHashMap();
        toEncode.put(SIGNER, gSigner);
        toEncode.put(SIGNATURE, gSignature);
        return JSONUtil.stringify(toEncode);
    }
}
