package com.greymass.esr.interfaces;

import com.greymass.esr.ESRException;

public interface IAbiProvider {
    String getAbi(String account) throws ESRException;
}
