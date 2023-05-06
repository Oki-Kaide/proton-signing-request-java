package com.greymass.esr;

import com.greymass.esr.interfaces.ISignatureProvider;
import com.greymass.esr.models.Signature;

import static com.greymass.esr.ESRTest.FOO;

public class MockSignatureProvider implements ISignatureProvider {

    public static final String MOCK_SIGNATURE = "SIG_K1_K8Wm5AXSQdKYVyYFPCYbMZurcJQXZaSgXoqXAKE6uxR6Jot7otVzS55JGRhixCwNGxaGezrVckDgh88xTsiu4wzzZuP9JE";

    @Override
    public Signature sign(String message) {
        return new Signature(FOO, MOCK_SIGNATURE);
    }
}
