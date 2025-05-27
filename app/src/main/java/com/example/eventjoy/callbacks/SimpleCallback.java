package com.example.eventjoy.callbacks;

public interface SimpleCallback {
    void onSuccess(String message);
    void onCancelled(String errorMessage);
}
