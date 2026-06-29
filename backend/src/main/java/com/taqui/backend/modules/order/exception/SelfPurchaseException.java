package com.taqui.backend.modules.order.exception;

public class SelfPurchaseException extends RuntimeException {

    public SelfPurchaseException(String message) {
        super(message);
    }
}
