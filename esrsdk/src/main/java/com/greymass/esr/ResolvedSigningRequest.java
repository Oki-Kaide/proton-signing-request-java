package com.greymass.esr;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.ResolvedCallback;
import com.greymass.esr.models.Transaction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.greymass.esr.models.ResolvedCallback.BN;
import static com.greymass.esr.models.ResolvedCallback.EX;
import static com.greymass.esr.models.ResolvedCallback.RBN;
import static com.greymass.esr.models.ResolvedCallback.REQ;
import static com.greymass.esr.models.ResolvedCallback.RID;
import static com.greymass.esr.models.ResolvedCallback.SA;
import static com.greymass.esr.models.ResolvedCallback.SIG;
import static com.greymass.esr.models.ResolvedCallback.SP;
import static com.greymass.esr.models.ResolvedCallback.TX;

public class ResolvedSigningRequest {

    private SigningRequest gSigningRequest;
    private PermissionLevel gSigner;
    private Transaction gTransaction;
    private byte[] gSerializedTransaction;

    public ResolvedSigningRequest(SigningRequest request, PermissionLevel signer, Transaction transaction, byte[] serializedTransaction) {
        gSigningRequest = request;
        gSigner = signer;
        gTransaction = transaction;
        gSerializedTransaction = serializedTransaction;
    }

    public byte[] getSerializedTransaction() {
        return Arrays.copyOf(gSerializedTransaction, gSerializedTransaction.length);
    }

    public String getTransactionId() throws ESRException {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(gSerializedTransaction);
            return BaseEncoding.base16().encode(sha256);
        } catch (NoSuchAlgorithmException e) {
            throw new ESRException("Failed to get SHA256 message digest");
        }
    }

    public ResolvedCallback getCallback(List<String> signatures) throws ESRException {
        return getCallback(signatures, -1);
    }

    public ResolvedCallback getCallback(List<String> signatures, long blockNum) throws ESRException {
        if (Strings.isNullOrEmpty(gSigningRequest.getCallback()))
            throw new ESRException("Callback is null or empty");

        Map<String, String> payload = Maps.newHashMap();
        payload.put(SIG, signatures.get(0));
        payload.put(TX, getTransactionId());
        payload.put(RBN, gTransaction.getRefBlockNum().toString());
        payload.put(RID, gTransaction.getRefBlockPrefix().toString());
        payload.put(EX, gTransaction.getExpiration());
        payload.put(REQ, gSigningRequest.encode());
        payload.put(SA, gSigner.getAccountName().getName());
        payload.put(SP, gSigner.getPermissionName().getName());

        for (int i = 1; i < signatures.size(); i++)
            payload.put(SIG + i, signatures.get(i));

        if (blockNum != -1)
            payload.put(BN, Long.toString(blockNum));

        Pattern pattern = Pattern.compile("(\\{\\{([a-z0-9]+)\\}\\})");
        Matcher matcher = pattern.matcher(gSigningRequest.getCallback());
        StringBuffer url = new StringBuffer(gSigningRequest.getCallback().length());
        while (matcher.find()) {
            String text = matcher.group(0);
            text = text.substring(2);
            text = text.substring(0, text.length() - 2);
            matcher.appendReplacement(url, payload.containsKey(text) ? String.valueOf(payload.get(text)) : "");
        }
        matcher.appendTail(url);
        String callbackUrl = url.toString();
        return new ResolvedCallback(callbackUrl, gSigningRequest.getRequestFlag().isBackground(), payload);
    }
}
