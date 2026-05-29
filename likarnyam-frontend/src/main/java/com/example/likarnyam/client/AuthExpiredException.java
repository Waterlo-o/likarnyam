package com.example.likarnyam.client;

public class AuthExpiredException extends RuntimeException {
    public AuthExpiredException(String message) {
        super(message);
    }
}