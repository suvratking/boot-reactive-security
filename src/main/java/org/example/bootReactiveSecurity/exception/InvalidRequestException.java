package org.example.bootReactiveSecurity.exception;

public class InvalidRequestException extends RuntimeException {
  public InvalidRequestException(String message) {
    super(message);
  }
}
