package com.easyliveline.streamingbackend.exceptions;

import lombok.Getter;

@Getter
public class CustomQueryException extends RuntimeException {
  private final String errorType;
  private final String clientMessage;

  public CustomQueryException(String errorType, String clientMessage, String message, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
    this.clientMessage = clientMessage;
  }

}