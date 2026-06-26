package com.taqui.backend.modules.post.exception;

public class InvalidPostException extends RuntimeException {
    public InvalidPostException(String message) {
        super(message);
    }
}
