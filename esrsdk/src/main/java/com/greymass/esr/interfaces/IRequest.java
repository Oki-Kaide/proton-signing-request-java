package com.greymass.esr.interfaces;

import com.greymass.esr.models.Action;

import java.util.List;

public interface IRequest {

    List<Action> getRawActions();

    List<Object> toVariant();
}
