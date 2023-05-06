package com.greymass.esr.implementation;

import com.greymass.esr.ESRException;
import com.greymass.esr.interfaces.IAbiProvider;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SimpleABIProvider implements IAbiProvider {

    private String gEndpointUrl;

    public SimpleABIProvider(String endpointUrl) {
        gEndpointUrl = endpointUrl;
    }

    @Override
    public String getAbi(String account) throws ESRException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), String.format("{\"account_name\": \"%s\"}", account));
        Request request = new Request.Builder().url(gEndpointUrl).post(body).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            return response.body().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
