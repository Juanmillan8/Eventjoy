package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;

import java.util.List;

public interface ValorationsCallback {
    void onSuccess(List<Valoration> valorations);
    void onFailure(Exception e);
}
