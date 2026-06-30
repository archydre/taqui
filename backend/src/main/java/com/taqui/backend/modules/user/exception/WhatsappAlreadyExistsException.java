package com.taqui.backend.modules.user.exception;

public class WhatsappAlreadyExistsException extends RuntimeException {

    public WhatsappAlreadyExistsException(String message) {
        super(message);
    }
}
