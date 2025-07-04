package com.ecoinsight.bdsp.asd.service;

public class ServiceRuntimeException extends RuntimeException {
  

  
    public ServiceRuntimeException(String message) {
        super(message);
    }

    public ServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
