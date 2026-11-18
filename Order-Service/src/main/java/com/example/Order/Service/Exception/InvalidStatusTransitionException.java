package com.example.Order.Service.Exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) { super(message); }
}
