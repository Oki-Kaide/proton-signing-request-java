package com.greymass.esr.models;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.greymass.esr.util.JSONUtil;

import java.util.Map;

import static com.greymass.esr.models.Transaction.EXPIRATION;
import static com.greymass.esr.models.Transaction.REF_BLOCK_NUM;
import static com.greymass.esr.models.Transaction.REF_BLOCK_PREFIX;

public class TransactionContext {

    private static final String TIMESTAMP = "timestamp";
    private static final String EXPIRE_SECONDS = "expire_seconds";
    private static final String BLOCK_NUM = "block_num";
    private String gTimestamp;
    private Integer gExpireSeconds = 60;
    private Long gBlockNum;
    private Long gRefBlockNum;
    private Long gRefBlockPrefix;
    private String gExpiration;

    public TransactionContext() {

    }

    public void setTimestamp(String timestamp) {
        gTimestamp = timestamp;
    }

    public String getTimestamp() {
        return gTimestamp;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        gExpireSeconds = expireSeconds;
    }

    public Integer getExpireSeconds() {
        return gExpireSeconds;
    }

    public void setBlockNum(Long blockNum) {
        gBlockNum = blockNum;
    }

    public Long getBlockNum() {
        return gBlockNum;
    }

    public void setRefBlockNum(Long refBlockNum) {
        gRefBlockNum = refBlockNum;
    }

    public Long getRefBlockNum() {
        return gRefBlockNum;
    }

    public void setRefBlockPrefix(Long refBlockPrefix) {
        gRefBlockPrefix = refBlockPrefix;
    }

    public Long getRefBlockPrefix() {
        return gRefBlockPrefix;
    }

    public void setExpiration(String expiration) {
        gExpiration = expiration;
    }

    public String getExpiration() {
        return gExpiration;
    }

    public String toJSON() {
        Map<String, Object> toEncode = Maps.newHashMap();
        toEncode.put(TIMESTAMP, getTimestamp());
        toEncode.put(EXPIRE_SECONDS, getExpireSeconds());
        toEncode.put(BLOCK_NUM, getBlockNum());
        toEncode.put(REF_BLOCK_NUM, getRefBlockNum());
        toEncode.put(REF_BLOCK_PREFIX, getRefBlockPrefix());
        toEncode.put(EXPIRATION, getExpiration());

        return JSONUtil.stringify(toEncode);
    }
}
