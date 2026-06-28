package com.taqui.backend.modules.user.exception;

public class WhatsappRequiredException extends RuntimeException {

    public WhatsappRequiredException(String message) {
        super(message);
    }
}
