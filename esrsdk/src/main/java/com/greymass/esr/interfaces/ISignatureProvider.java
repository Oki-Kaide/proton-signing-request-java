package com.greymass.esr.interfaces;

import com.greymass.esr.models.Signature;

public interface ISignatureProvider {

    Signature sign(String message);

}
