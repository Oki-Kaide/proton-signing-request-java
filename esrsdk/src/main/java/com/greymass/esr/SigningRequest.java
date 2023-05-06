package com.greymass.esr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.greymass.esr.interfaces.IAbiProvider;
import com.greymass.esr.interfaces.IRequest;
import com.greymass.esr.interfaces.ISignatureProvider;
import com.greymass.esr.models.AccountName;
import com.greymass.esr.models.Action;
import com.greymass.esr.models.Actions;
import com.greymass.esr.models.Chain;
import com.greymass.esr.models.ChainId;
import com.greymass.esr.models.IRequestFactory;
import com.greymass.esr.models.Identity;
import com.greymass.esr.models.InfoPair;
import com.greymass.esr.models.LinkCreate;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.PermissionName;
import com.greymass.esr.models.RequestFlag;
import com.greymass.esr.models.SealedMessage;
import com.greymass.esr.models.Signature;
import com.greymass.esr.models.Transaction;
import com.greymass.esr.models.TransactionContext;
import com.greymass.esr.util.CompressionUtil;
import com.greymass.esr.util.JSONUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import static com.greymass.esr.models.ChainId.CHAIN_ID;

public class SigningRequest {

    public static final int PROTOCOL_VERSION = 2;
    public static final String PLACEHOLDER_NAME = "............1";
    public static final String PLACEHOLDER_PERMISSION = "............2";
    public static final PermissionLevel PLACEHOLDER_PERMISSION_LEVEL = new PermissionLevel(PLACEHOLDER_NAME, PLACEHOLDER_PERMISSION);
    public static final String PLACEHOLDER_PACKED = "0101000000000000000200000000000000";

    private static final String REQ = "req";
    private static final String FLAGS = "flags";
    private static final String CALLBACK = "callback";
    private static final String INFO = "info";
    private static final String SIG = "sig";
    private IAbiProvider gAbiProvider;
    private ESRV8Runtime gRuntime;
    private ResourceReader gResourceReader;
    private ESR gESR;
    private ChainId gChainId;
    private IRequest gRequest;
    private RequestFlag gRequestFlag;
    private String gCallback;
    private List<InfoPair> gInfoPairs;
    private Signature gSignature;

    public SigningRequest(ESR esr) {
        gESR = esr;
        gResourceReader = esr.getResourceReader();
        gAbiProvider = esr.getAbiProvider();
        gRuntime = esr.getRuntime();
        gChainId = new ChainId(Chain.EOS);
        gRequestFlag = RequestFlag.getDefault();
        gCallback = "";
        gInfoPairs = Lists.newArrayList();
    }

    public SigningRequest load(String uri) throws ESRException {
        String[] parts = uri.split(":");
        String prefix = parts[0];
        String path = parts[1];

        if (!prefix.equals("esr") && !prefix.equals("web+esr"))
            throw new ESRException("Only esr and web+esr schemes are supported");

        if (path.startsWith("//"))
            path = path.substring(2);


        byte[] decoded = gRuntime.base64uDecode(path);

        return load(decoded);
    }

    SigningRequest load(byte[] data) throws ESRException {
        int header = data[0] & 0xff;
        int version = header & ~(1 << 7);
        if (version != PROTOCOL_VERSION)
            throw new ESRException("Unsupported protocol version");

        byte[] reqArray = Arrays.copyOfRange(data, 1, data.length);
        if ((header & (1 << 7)) != 0) {
            try {
                reqArray = CompressionUtil.decompressByteArray(reqArray);
            } catch (DataFormatException | IOException e) {
                e.printStackTrace();
                throw new ESRException("Failed to decompress request: " + e.getMessage());
            }
        }

        String requestJson = gRuntime.deserializeSigningRequest(reqArray);
        JsonObject result = (JsonObject) JsonParser.parseString(requestJson);
        JsonObject request = result.getAsJsonObject(REQ);
        gChainId = ChainId.fromVariant(request.getAsJsonArray(CHAIN_ID));
        gRequest = IRequestFactory.fromVariant(request.getAsJsonArray(REQ));
        gRequestFlag = new RequestFlag(request.get(FLAGS).getAsByte());
        gCallback = request.get(CALLBACK).getAsString();
        gInfoPairs = InfoPair.listFromDeserializedJsonArray(request.get(INFO).getAsJsonArray());
        if (result.has(SIG) && result.get(SIG).isJsonObject())
            gSignature = new Signature(result.getAsJsonObject(SIG));
        return this;
    }

    public void setRequest(Action action) throws ESRException {
        if (!action.getData().isPacked())
            gRuntime.serializeActionData(gAbiProvider, action);

        gRequest = action;
    }

    public void setRequest(Actions actions) throws ESRException {
        for (Action action : actions.getActions()) {
            if (!action.getData().isPacked())
                gRuntime.serializeActionData(gAbiProvider, action);
        }

        gRequest = actions;
    }

    public void setRequest(Transaction transaction) throws ESRException {
        for (Action action : transaction.getActionsList()) {
            if (!action.getData().isPacked())
                gRuntime.serializeActionData(gAbiProvider, action);
        }

        for (Action action : transaction.getContextFreeActionsList()) {
            if (!action.getData().isPacked())
                gRuntime.serializeActionData(gAbiProvider, action);
        }

        gRequest = transaction;
    }

    public void setRequest(Identity identity) {
        gRequest = identity;
    }

    public void setSignature(Signature signature) {
        gSignature = signature;
    }

    public boolean isIdentity() {
        return gRequest != null && (gRequest instanceof Identity);
    }

    public String getIdentity() {
        if (isIdentity() && ((Identity) gRequest).getPermissionLevel() != null) {
            AccountName accountName = ((Identity) gRequest).getPermissionLevel().getAccountName();
            if (accountName == null)
                return null;

            String actor = accountName.getName();
            return PLACEHOLDER_NAME.equals(actor) ? null : actor;
        }

        return null;
    }

    public String getIdentityPermission() {
        if (isIdentity() && ((Identity) gRequest).getPermissionLevel() != null) {
            PermissionName permissionName = ((Identity) gRequest).getPermissionLevel().getPermissionName();
            if (permissionName == null)
                return null;

            String permission = permissionName.getName();
            return PLACEHOLDER_NAME.equals(permission) ? null : permission;
        }

        return null;
    }

    public boolean hasSignature() {
        return gSignature != null;
    }

    public Signature getSignature() {
        return gSignature;
    }

    public void sign(ISignatureProvider signatureProvider) {
        setSignature(signatureProvider.sign(getSignatureDigestAsHex()));
    }

    public String encode() {
        return encode(true, true);
    }

    public String encode(boolean compress, boolean slashes) {
        byte header = PROTOCOL_VERSION;
        byte[] data = getData();
        byte[] sigData = getSignatureData();
        byte[] toEncode = Bytes.concat(data, sigData);
        if (compress) {
            byte[] compressed = CompressionUtil.compressByteArray(toEncode);
            if (toEncode.length > compressed.length) {
                header |= 1 << 7;
                toEncode = compressed;
            }
        }

        byte[] out = Bytes.concat(new byte[]{header}, toEncode);
        String scheme = "esr:";
        if (slashes)
            scheme += "//";


        return scheme + gRuntime.base64uEncode(out);
    }

    public List<Action> resolveActions() throws ESRException {
        return resolveActions(fetchAbis());
    }

    public List<Action> resolveActions(PermissionLevel signer) throws ESRException {
        return resolveActions(fetchAbis(), signer);
    }

    public List<Action> resolveActions(Map<String, String> abiMap) throws ESRException {
        return resolveActions(abiMap, null);
    }

    public List<Action> resolveActions(Map<String, String> abiMap, PermissionLevel signer) throws ESRException {
        List<Action> rawActions = getRawActions();
        List<Action> resolvedActions = Lists.newArrayList();
        for (Action raw : rawActions) {
            resolvedActions.add(gRuntime.getResolvedAction(abiMap, signer, raw));
        }
        return resolvedActions;
    }

    public Transaction resolveTransaction(PermissionLevel signer) throws ESRException {
        return resolveTransaction(fetchAbis(), signer);
    }

    public Transaction resolveTransaction(PermissionLevel signer, TransactionContext transactionContext) throws ESRException {
        return resolveTransaction(fetchAbis(), signer, transactionContext);
    }

    public Transaction resolveTransaction(Map<String, String> abiMap, PermissionLevel signer) throws ESRException {
        return resolveTransaction(abiMap, signer, new TransactionContext());
    }

    public Transaction resolveTransaction(Map<String, String> abiMap, PermissionLevel signer, TransactionContext transactionContext) throws ESRException {
        Transaction transaction = getRawTransaction();
        if (!(gRequest instanceof Identity) && !transaction.hasTapos()) {
            if (transactionContext.getExpiration() != null &&
                    transactionContext.getRefBlockNum() != null &&
                    transactionContext.getRefBlockPrefix() != null) {
                transaction.setExpiration(transactionContext.getExpiration());
                transaction.setRefBlockNum(transactionContext.getRefBlockNum());
                transaction.setRefBlockPrefix(transactionContext.getRefBlockPrefix());
            } else if (transactionContext.getBlockNum() != null &&
                        transactionContext.getRefBlockPrefix() != null &&
                        transactionContext.getTimestamp() != null) {
                gRuntime.setTransactionFromContext(transaction, transactionContext);
            } else {
                throw new ESRException("Invalid transaction context, need either a reference block or explicit TAPoS values");
            }
        }
        List<Action> actions = resolveActions(abiMap, signer);

        Transaction resolved = transaction.shallowClone();
        resolved.setActions(actions);
        return resolved;
    }

    public ResolvedSigningRequest resolve(PermissionLevel signer, TransactionContext transactionContext) throws ESRException {
        return resolve(fetchAbis(), signer, transactionContext);
    }

    public ResolvedSigningRequest resolve(Map<String, String> abiMap, PermissionLevel signer, TransactionContext transactionContext) throws ESRException {
        Transaction transaction = resolveTransaction(signer, transactionContext);
        for (Action action : transaction.getActionsList())
            gRuntime.serializeActionData(gAbiProvider, action);

        byte[] serializedTransaction = gRuntime.serializeTransaction(transaction.toJSON());
        return new ResolvedSigningRequest(this, signer, transaction, serializedTransaction);
    }

    public Transaction getRawTransaction() throws ESRException {
        if (gRequest == null)
            throw new ESRException("Cannot get raw transaction, request is not set");

        if (gRequest instanceof Transaction)
            return (Transaction) gRequest;

        Transaction transaction = new Transaction();
        transaction.setActions(getRawActions());
        return transaction;
    }

    public List<Action> getRawActions() {
        if (gRequest instanceof Identity)
            return Lists.newArrayList(gRuntime.identityToAction((Identity) gRequest));

        return gRequest.getRawActions();
    }

    public List<String> getRequiredAbis() {
        List<String> accounts = Lists.newArrayList();
        for (Action action : getRawActions()) {
            if (!action.isIdentity())
                accounts.add(action.getAccount().getName());
        }

        return accounts;
    }

    public Map<String, String> fetchAbis() throws ESRException {
        Map<String, String> abiMap = Maps.newHashMap();
        for (String accountName : getRequiredAbis())
            abiMap.put(accountName, gAbiProvider.getAbi(accountName));

        return abiMap;
    }

    public ChainId getChainId() {
        return gChainId;
    }

    public void setChainId(ChainId chainId) {
        gChainId = chainId;
    }

    public byte[] getData() {
        return gRuntime.serializeSigningRequest(this.toDataJSON());
    }

    public byte[] getSignatureData() {
        if (gSignature == null)
            return new byte[0];

        return gRuntime.getSignatureData(gSignature);
    }

    public String getSignatureDigestAsHex() {
        return gRuntime.getSignatureDigestAsHex(PROTOCOL_VERSION, getData());
    }

    public IRequest getRequest() {
        return gRequest;
    }

    public String getCallback() {
        return gCallback;
    }

    public void setCallback(String callback) {
        gCallback = callback;
    }

    public RequestFlag getRequestFlag() {
        return gRequestFlag;
    }

    public void setRequestFlag(RequestFlag requestFlag) {
        gRequestFlag = requestFlag;
    }

    public void setInfoKey(String key, Object value) throws ESRException {
        if (key == null)
            throw new ESRException("Key cannot be null");

        String hexValue;
        if (value instanceof String) {
            String stringVal = (String) value;
            hexValue = BaseEncoding.base16().encode(stringVal.getBytes());
        } else if (value instanceof Boolean) {
            boolean booleanValue = (boolean) value;
            hexValue = BaseEncoding.base16().encode(booleanValue ? new byte[]{1} : new byte[]{0});
        } else {
            throw new ESRException("Can only setInfoKey with string or boolean");
        }
        for (InfoPair pair : gInfoPairs) {
            if (key.equals(pair.getKey())) {
                pair.setHexValue(hexValue);
                return;
            }
        }

        gInfoPairs.add(new InfoPair(key, hexValue));
    }

    public List<InfoPair> getInfoPairs() {
        return gInfoPairs;
    }

    public Map<String, byte[]> getRawInfo() {
        Map<String, byte[]> rawInfo = Maps.newLinkedHashMap();
        for (InfoPair pair : gInfoPairs)
            rawInfo.put(pair.getKey(), pair.getBytesValue());

        return rawInfo;
    }

    public Map<String, String> getInfo() {
        Map<String, String> info = Maps.newLinkedHashMap();
        for (InfoPair pair : gInfoPairs)
            info.put(pair.getKey(), pair.getStringValue());

        return info;
    }

    public LinkCreate decodeLinkCreate(String encodedLinkCreate) {
        String linkCreateJson = gRuntime.deserializeLinkCreate(encodedLinkCreate);
        return new LinkCreate((JsonObject) JsonParser.parseString(linkCreateJson));
    }

    public SealedMessage decodeSealedMessage(String encodedSealedMessage) {
        String sealedMessageJson = gRuntime.deserializeSealedMessage(encodedSealedMessage);
        return new SealedMessage((JsonObject) JsonParser.parseString(sealedMessageJson));
    }

    public void addInfoPair(InfoPair infoPair) {
        gInfoPairs.add(infoPair);
    }

    public void setInfoPairs(List<InfoPair> infoPairs) {
        gInfoPairs = infoPairs;
    }

    public String toDataJSON() {
        Map<String, Object> toEncode = Maps.newHashMap();
        toEncode.put(CHAIN_ID, gChainId.toVariant());
        toEncode.put(REQ, gRequest.toVariant());
        toEncode.put(FLAGS, gRequestFlag.getFlagValue());
        toEncode.put(CALLBACK, gCallback);

        List<Object> info = Lists.newArrayList();
        for (InfoPair pair : gInfoPairs)
            info.add(pair.toMap());

        toEncode.put(INFO, info);

        return JSONUtil.stringify(toEncode);
    }

    public SigningRequest copy() throws ESRException {
        SigningRequest copy = new SigningRequest(gESR);
        copy.load(encode());
        return copy;
    }

}
