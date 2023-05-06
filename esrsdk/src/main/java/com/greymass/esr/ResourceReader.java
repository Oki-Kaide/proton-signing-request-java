package com.greymass.esr;

import android.content.Context;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class ResourceReader {
    private Context gContext;

    public ResourceReader(Context context) {
        gContext = context;
    }

    public String readResourceString(int identifier) {
        InputStream is = null;
        try {
            is = gContext.getResources().openRawResource(identifier);
            return new String(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {}
        }

        return null;
    }
}
