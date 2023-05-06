package com.greymass.esr;

import android.content.Context;

import com.greymass.esr.interfaces.IAbiProvider;

public class SigningRequestOptions {

   private Context gContext;
   private IAbiProvider gAbiProvider;

   public SigningRequestOptions(Context context, IAbiProvider abiProvider) {
      gContext = context;
      gAbiProvider = abiProvider;
   }

}
