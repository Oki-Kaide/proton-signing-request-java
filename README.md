# eosio-signing-request-java

A java/android library to assist with the EOSIO Signing Request (ESR) protocol, it can be found on bintray and dowloaded via jcenter here:

https://bintray.com/greymass/com.greymass.eosio-signing-request/eosio-signing-request-java

The full specification for ESR is available here:

https://github.com/eosio-eps/EEPs/blob/master/EEPS/eep-7.md

The ESR protocol allows for an application (dapp) to generate signature requests (transactions) which can then be passed to signers (wallets) for signature creation. These signature requests can be used within URI links, QR Codes, or other transports between applications and signers.

---

## Installation

To add esrsdk to your android project's gradle.build file:

```implementation 'com.greymass:esrsdk:1.0.7'```

---

## Signing Request Flow

In an environment where an ***application/dapp*** is requesting that an end user perform a transaction within their preferred ***signer/wallet***, each of these applications will utilize the `eosio-signing-request-java` library to fulfill different roles.

- The ***application/dapp*** will be creating and encoding the signing request.
- The ***signer/wallet*** will be decoding and resolving the signing request.

The specification itself then allows either the ***signer/wallet*** itself to broadcast the finalized transaction, or the transaction/signature themselves can be passed back to the ***application/dapp*** to broadcast.

The `eosio-signing-request` library is not responsible for transporting this information between the ***application/dapp***
and ***signer/wallet***, and so this topic will not be covered in this README.

---

## Usage Examples

As a ***signer/wallet*** if you receive an encoded ESR request, to decode the request:

NOTE: the below example assumes to be running from an Activity or a class where `this` is a Context, from other classes you'll need to pass a Context to `new ESR(context, abiProvider)`

```java
IAbiProvider abiProvider = new SimpleABIProvider("https://eos.greymass.com");
SigningRequest signingRequest = new SigningRequest(new ESR(this, abiProvider));
String esrUri = "esr://hexstring";
signingRequest.load(esrUri);

// get info pairs
Map<String, String> info = signingRequest.getInfo();
String foo = info.get("foo");

// check if this is a identity request
if (signingRequest.isIdentity()) {
    ResolvedSigningRequest resolved = signingRequest.resolve(new PermissionLevel("myaccount", "active"), new TransactionContext());
    ResolvedCallback callback = resolved.getCallback(new ArrayList<String>());
    // call the callback to notify of the request
} else {
    // it's a signing request
    signingRequest.sign(new ISignatureProvider() {
        @Override
        public Signature sign(String message) {
            // sign it
            return new Signature("myaccount", "SIG_abc123");
        }
    });

    ResolvedSigningRequest resolved = signingRequest.resolve(new PermissionLevel("myaccount", "active"), new TransactionContext());
    if (signingRequest.getRequestFlag().isBroadcast()) {
        // broadcast
    } else {
        // call the callback so requestor can broadcast
    }
}
```
