package com.greymass.esr.models;

public class TransactionHeader {
    public static final String DEFAULT_EXPIRATION = "1970-01-01T00:00:00.000";
    private String gExpiration = DEFAULT_EXPIRATION;
    private Long gRefBlockNum = 0L;
    private Long gRefBlockPrefix = 0L;
    private int gMaxNetUsageWords = 0;
    private int gMaxCpuUsageMs = 0;
    private int gDelaySec = 0;

    public TransactionHeader() {

    }

    public TransactionHeader(String expiration, Long refBlockNum,
                             Long refBlockPrefix, int maxNetUsageWords,
                             int maxCpuUsageMs, int delaySec) {
        gExpiration = expiration;
        gRefBlockNum = refBlockNum;
        gRefBlockPrefix = refBlockPrefix;
        gMaxNetUsageWords = maxNetUsageWords;
        gMaxCpuUsageMs = maxCpuUsageMs;
        gDelaySec = delaySec;
    }

    public String getExpiration() {
        return gExpiration;
    }

    public void setExpiration(String expiration) {
        gExpiration = expiration;
    }

    public Long getRefBlockNum() {
        return gRefBlockNum;
    }

    public void setRefBlockNum(Long refBlockNum) {
        gRefBlockNum = refBlockNum;
    }

    public Long getRefBlockPrefix() {
        return gRefBlockPrefix;
    }

    public void setRefBlockPrefix(Long refBlockPrefix) {
        gRefBlockPrefix = refBlockPrefix;
    }

    public int getMaxNetUsageWords() {
        return gMaxNetUsageWords;
    }

    public void setMaxNetUsageWords(int maxNetUsageWords) {
        gMaxNetUsageWords = maxNetUsageWords;
    }

    public int getMaxCpuUsageMs() {
        return gMaxCpuUsageMs;
    }

    public void setMaxCpuUsageMs(int maxCpuUsageMs) {
        gMaxCpuUsageMs = maxCpuUsageMs;
    }

    public int getDelaySec() {
        return gDelaySec;
    }

    public void setDelaySec(int delaySec) {
        gDelaySec = delaySec;
    }
}
