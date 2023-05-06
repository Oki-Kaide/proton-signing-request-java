package com.greymass.esr;

import android.content.Context;

import com.greymass.esr.interfaces.IAbiProvider;

public class ESR {
    private IAbiProvider gAbiProvider;
    private ESRV8Runtime gRuntime;
    private Context gContext;
    private ResourceReader gResourceReader;

    public ESR(Context context, IAbiProvider abiProvider) {
        gContext = context;
        gResourceReader = new ResourceReader(context);
        gAbiProvider = abiProvider;
        gRuntime = new ESRV8Runtime(gResourceReader);
    }

    protected ResourceReader getResourceReader() {
        return gResourceReader;
    }

    protected IAbiProvider getAbiProvider() {
        return gAbiProvider;
    }

    protected ESRV8Runtime getRuntime() {
        return gRuntime;
    }

}
