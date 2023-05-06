package com.greymass.esr.models;

public class RequestFlag {

    private byte gFlag;

    public RequestFlag(byte flag) {
        gFlag = flag;
    }

    public static RequestFlag getDefault() {
        return new RequestFlag((byte) 1);
    }

    public void setBroadcast(boolean b) {
        if (b)
            gFlag |= Flag.BROADCAST.gFlag;
        else
            gFlag &= ~Flag.BROADCAST.gFlag;
    }

    public boolean isBroadcast() {
        return (gFlag & Flag.BROADCAST.getFlag()) != 0;
    }

    public void setBackground(boolean b) {
        if (b)
            gFlag |= Flag.BACKGROUND.gFlag;
        else
            gFlag &= ~Flag.BACKGROUND.gFlag;
    }


    public boolean isBackground() {
        return (gFlag & Flag.BACKGROUND.getFlag()) != 0;
    }

    public byte getFlagValue() {
        return gFlag;
    }

    public enum Flag {
        NONE((byte) 0),
        BROADCAST((byte) (1 << 0)),
        BACKGROUND((byte) (1 << 1));

        private byte gFlag;

        Flag(byte flag) {
            gFlag = flag;
        }

        public byte getFlag() {
            return gFlag;
        }
    }

}
