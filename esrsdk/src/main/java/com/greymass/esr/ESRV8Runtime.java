package com.greymass.esr;

import android.util.Log;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8ArrayBuffer;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8TypedArray;
import com.eclipsesource.v8.V8Value;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.greymass.esr.interfaces.IAbiProvider;
import com.greymass.esr.models.AccountName;
import com.greymass.esr.models.Action;
import com.greymass.esr.models.ActionData;
import com.greymass.esr.models.ActionName;
import com.greymass.esr.models.Identity;
import com.greymass.esr.models.PermissionLevel;
import com.greymass.esr.models.Signature;
import com.greymass.esr.models.Transaction;
import com.greymass.esr.models.TransactionContext;
import com.greymass.esr.util.JSONUtil;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.greymass.esr.models.Transaction.EXPIRATION;
import static com.greymass.esr.models.Transaction.REF_BLOCK_NUM;
import static com.greymass.esr.models.Transaction.REF_BLOCK_PREFIX;

public class ESRV8Runtime {
	private static final String V8_TAG = "ESR-V8";
	private static final String UNDEFINED = "undefined";
	private V8 gRuntime;
	private ResourceReader gResourceReader;

	public ESRV8Runtime(ResourceReader resourceReader) {
		gResourceReader = resourceReader;
		gRuntime = V8.createV8Runtime("global");
		gRuntime.executeVoidScript(gResourceReader.readResourceString(R.raw.serialize));
		injectConsole();
		injectEncoderDecoder();
		injectGetContract();
	}

	public byte[] base64uDecode(String b64data) {
		V8Array array = gRuntime.executeArrayScript(String.format("(function() {\n" +
			"let decoded = global.base64u.decode('%s')\n" +
			"return decoded\n" +
			"})()", b64data));
		return v8ArrayToByteArray(array);
	}

	public String base64uEncode(byte[] bytes) {
		return gRuntime.executeStringScript(String.format("(function() {\n" +
			"let decoded = global.base64u.encode(%s)\n" +
			"return decoded\n" +
			"})()", byteArrayToUnt8ScriptValue(bytes)));
	}

	public String deserializeSigningRequest(byte[] data) {
		String script = String.format("(() => {\n" +
			"let array = Uint8Array.from(%s)\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder,\n" +
			"    array,\n" +
			"})\n" +
			"const req = AbiTypes.get('signing_request').deserialize(buffer)\n" +
			"let signature = global.abi.RequestSignature | undefined\n" +
			"if (buffer.haveReadData()) {\n" +
			"    const type = global.AbiTypes.get('request_signature')\n" +
			"    signature = type.deserialize(buffer)\n" +
			"}\n" +
			"return JSON.stringify({\n" +
			"    req: req,\n" +
			"    sig: signature\n" +
			"})" +
			"})()", JSONUtil.stringify(data));
		return gRuntime.executeStringScript(script);
	}

	public String deserializeLinkCreate(String serializedLink) {
		String script = String.format("let array = Uint8Array.from(%s)\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder,\n" +
			"    array,\n" +
			"})\n" +
			"const linkCreate = AbiTypes.get('link_create').deserialize(buffer)\n" +
			"JSON.stringify(linkCreate)\n", JSONUtil.stringify(decodeHexString(serializedLink)));
		return gRuntime.executeStringScript(script);
	}

	public String deserializeSealedMessage(String serializedSealedMessage) {
		String script = String.format("let array = Uint8Array.from(%s)\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder,\n" +
			"    array,\n" +
			"})\n" +
			"const sealedMessage = AbiTypes.get('sealed_message').deserialize(buffer)\n" +
			"JSON.stringify(sealedMessage)\n", JSONUtil.stringify(decodeHexString(serializedSealedMessage)));
		return gRuntime.executeStringScript(script);
	}

	public void serializeActionData(IAbiProvider abiProvider, Action action) throws ESRException {
		if (action.getData().isPacked())
			return;

		String abi = abiProvider.getAbi(action.getAccount().getName());
		abi = abi.replace("%", "%%");

		String script = String.format("(function() {\n" +
				"let contract = " +
				(action.isIdentity() ? "global.getContract(global.abi.data)" : "global.getContract(" + abi + ")") + "\n" +
				"let serializedActionData = global.Serialize.serializeActionData(contract, '%s', '%s', %s, {encode: global.encodeFunction}, {decode: global.decodeFunction})\n" +
				"return serializedActionData\n" +
				"})()",
			action.getAccount().getName(),
			action.getName().getName(),
			action.getData().toJSON());
		String packed = gRuntime.executeStringScript(script);
		action.getData().setData(packed);
	}

	public byte[] serializeTransaction(String transactionJSON) {
		String script = String.format("(function() {\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder\n" +
			"})\n" +
			"let data = %s\n" +
			"global.transactionType.serialize(buffer, data)\n" +
			"return buffer.asUint8Array()\n" +
			"})()", transactionJSON);
		V8Array obj = gRuntime.executeArrayScript(script);
		return v8ArrayToByteArray(obj);
	}

	public Action identityToAction(Identity identity) {
		Action action = new Action();
		action.setAccount(new AccountName(""));
		action.setName(new ActionName(Identity.IDENTITY));

		PermissionLevel permissionLevel = identity.getPermissionLevel();
		if (permissionLevel == null || permissionLevel.getAccountName() == null ||
			Strings.isNullOrEmpty(permissionLevel.getAccountName().getName()) ||
			permissionLevel.getPermissionName() == null ||
			Strings.isNullOrEmpty(permissionLevel.getPermissionName().getName())) {
			action.addAuthorization(SigningRequest.PLACEHOLDER_PERMISSION_LEVEL);
			action.setData(new ActionData(SigningRequest.PLACEHOLDER_PACKED));
		} else {
			String script = String.format("(function() {\n" +
				"let identity = {permission:{actor:'%s',permission:'%s'}}\n" +
				"const textEncoder = {encode: global.encodeFunction}\n" +
				"const textDecoder = {decode: global.decodeFunction}\n" +
				"const buffer = new Serialize.SerialBuffer({\n" +
				"    textEncoder,\n" +
				"    textDecoder\n" +
				"})\n" +
				"global.idType.serialize(buffer, identity)\n" +
				"return Serialize.arrayToHex(buffer.asUint8Array())" +
				"})()", permissionLevel.getAccountName().getName(), permissionLevel.getPermissionName().getName());
			String packedData = gRuntime.executeStringScript(script);
			action.addAuthorization(identity.getPermissionLevel());
			action.setData(new ActionData(packedData));
		}

		return action;
	}

	public void setTransactionFromContext(Transaction transaction, TransactionContext context) {
		String script = String.format("(function() {\n" +
			"return Serialize.transactionHeader(%s, %d)\n" +
			"})()", context.toJSON(), context.getExpireSeconds() != null ? context.getExpireSeconds() : 60);
		V8Object result = gRuntime.executeObjectScript(script);
		transaction.setExpiration(result.getString(EXPIRATION));
		transaction.setRefBlockNum((long) result.getInteger(REF_BLOCK_NUM));
		transaction.setRefBlockPrefix((long) result.getInteger(REF_BLOCK_PREFIX));
	}

	public Action getResolvedAction(Map<String, String> abiMap, PermissionLevel signer, Action raw) throws ESRException {
		String abi = null;
		if (!raw.getData().isPacked())
			throw new ESRException("Cannot resolve an already resolved action");

		if (!raw.isIdentity()) {
			abi = abiMap.get(raw.getAccount().getName());
			if (abi == null)
				throw new ESRException("Missing ABI definition for " + raw.getAccount().getName());

		} else {
			abi = "global.abi.data";
		}
		String script = String.format("(function() {\n" +
				"const contractAbi = %s\n" +
				"const signer = %s\n" +
				"const PlaceholderName = '%s'\n" +
				"const PlaceholderPermission = '%s'\n" +
				"const contract = getContract(contractAbi)\n" +
				"if (signer) {\n" +
				"   contract.types.get('name').deserialize = (buffer) => {\n" +
				"       const name = buffer.getName()\n" +
				"       if (name === PlaceholderName) {\n" +
				"          return signer.actor\n" +
				"       } else if (name === PlaceholderPermission) {\n" +
				"           return signer.permission\n" +
				"       } else {\n" +
				"           return name\n" +
				"       }\n" +
				"   }\n" +
				"}\n" +
				"const textEncoder = {encode: global.encodeFunction}\n" +
				"const textDecoder = {decode: global.decodeFunction}\n" +
				"const action = Serialize.deserializeAction(\n" +
				"    contract,\n" +
				"    '%s',\n" +
				"    '%s',\n" +
				"    %s,\n" +
				"    '%s',\n" +
				"    textEncoder,\n" +
				"    textDecoder\n" +
				")\n" +
				"if (signer) {\n" +
				"    action.authorization = action.authorization.map((auth) => {\n" +
				"        let { actor, permission } = auth\n" +
				"        if (actor === PlaceholderName) {\n" +
				"            actor = signer.actor\n" +
				"        }\n" +
				"        if (permission === PlaceholderPermission) {\n" +
				"            permission = signer.permission\n" +
				"        }\n" +
				"        if (permission === PlaceholderName) {\n" +
				"            permission = signer.permission\n" +
				"        }\n" +
				"        return { actor, permission }\n" +
				"    })\n" +
				"}\n" +
				"return JSON.stringify(action)\n" +
				"})()", abi, signer != null ? signer.toJSON() : UNDEFINED, SigningRequest.PLACEHOLDER_NAME, SigningRequest.PLACEHOLDER_PERMISSION,
			raw.getAccount().getName(), raw.getName().getName(), raw.getAuthorizationJSON(), raw.getData().getPackedData());

		String resolvedActionJSON = gRuntime.executeStringScript(script);
		return new Action((JsonObject) JsonParser.parseString(resolvedActionJSON));
	}

	public byte[] getSignatureData(Signature signature) {
		String script = String.format("(function() {\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder\n" +
			"})\n" +
			"const type = AbiTypes.get('request_signature')\n" +
			"type.serialize(buffer, %s)\n" +
			"return buffer.asUint8Array()\n" +
			"})()", signature.toJSON());
		return v8ArrayToByteArray(gRuntime.executeArrayScript(script));
	}

	public byte[] serializeSigningRequest(String requestJSON) {
		String script = String.format("(function() {\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder\n" +
			"})\n" +
			"let data = %s\n" +
			"global.signingRequestType.serialize(buffer, data)\n" +
			"return buffer.asUint8Array()\n" +
			"})()", requestJSON);
		V8Array obj = gRuntime.executeArrayScript(script);
		return v8ArrayToByteArray(obj);
	}

	public String getSignatureDigestAsHex(int protocolVersion, byte[] data) {
		String script = String.format("(function() {\n" +
			"const textEncoder = {encode: global.encodeFunction}\n" +
			"const textDecoder = {decode: global.decodeFunction}\n" +
			"const buffer = new Serialize.SerialBuffer({\n" +
			"    textEncoder,\n" +
			"    textDecoder\n" +
			"})\n" +
			"// protocol version + utf8 \"request\"\n" +
			"buffer.pushArray([%d, 0x72, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74])\n" +
			"let data = %s\n" +
			"buffer.pushArray(data)\n" +
			"let bufferArray = buffer.asUint8Array()\n" +
			"return Serialize.arrayToHex(sha256(bufferArray))\n" +
			"})()", protocolVersion, byteArrayToUnt8ScriptValue(data));
		return gRuntime.executeStringScript(script);
	}

	private void injectConsole() {
		JavaCallback log = new JavaCallback() {
			@Override
			public Object invoke(V8Object v8Object, V8Array v8Array) {
				switch (v8Array.getString(0)) {
					case "debug":
						Log.d(V8_TAG, v8Array.getString(1));
						break;
					case "error":
						Log.e(V8_TAG, v8Array.getString(1));
						break;
					default:
						Log.i(V8_TAG, v8Array.getString(1));
				}
				return null;
			}
		};

		gRuntime.registerJavaMethod(log, "JavaLogger");
		gRuntime.executeVoidScript(gResourceReader.readResourceString(R.raw.console));
	}

	private void injectEncoderDecoder() {
		JavaCallback encode = new JavaCallback() {
			@Override
			public Object invoke(V8Object v8Object, V8Array v8Array) {
				String toEncode = v8Array.getString(0);
				byte[] bytes = toEncode.getBytes(Charsets.UTF_8);
				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
				byteBuffer.put(bytes);
				V8ArrayBuffer buffer = new V8ArrayBuffer(gRuntime, byteBuffer);
				return new V8TypedArray(gRuntime, buffer, V8Value.UNSIGNED_INT_8_ARRAY, 0, bytes.length);
			}
		};

		JavaCallback decode = new JavaCallback() {
			@Override
			public Object invoke(V8Object v8Object, V8Array v8Array) {
				V8Array toDecode = v8Array.getArray(0);
				byte[] toDecodeBytes = toDecode.getBytes(0, toDecode.length());
				return new String(toDecodeBytes, Charsets.US_ASCII);
			}
		};

		gRuntime.registerJavaMethod(encode, "encodeFunction");
		gRuntime.registerJavaMethod(decode, "decodeFunction");
		gRuntime.executeVoidScript(gResourceReader.readResourceString(R.raw.encoding));
	}

	private void injectGetContract() {
		gRuntime.executeVoidScript(gResourceReader.readResourceString(R.raw.getcontract));
	}

	private byte[] v8ArrayToByteArray(V8Array array) {
		return array.getBytes(0, array.length());
	}

	private int[] v8ArrayToIntArray(V8Array array) {
		return array.getIntegers(0, array.length());
	}

	private String byteArrayToUnt8ScriptValue(byte[] from) {
		return String.format("Uint8Array.from(%s)", JSONUtil.stringify(from));
	}

	private byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException(
				"Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	public byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if(digit == -1) {
			throw new IllegalArgumentException(
				"Invalid Hexadecimal Character: "+ hexChar);
		}
		return digit;
	}

}
