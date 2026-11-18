package com.example.Order.Service.Exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) { super(message); }
}
