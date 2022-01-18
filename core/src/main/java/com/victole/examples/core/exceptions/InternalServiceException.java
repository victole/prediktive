package com.victole.examples.core.exceptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal Service Exception - internal exception class.
 */
public class InternalServiceException extends Exception {

    private static final long serialVersionUID = 1L;
    private final Integer errorCode;
    private String errorMessage = StringUtils.EMPTY;
    private String errorDescription = StringUtils.EMPTY;
    private transient List<ErrorDetails> errorDetails = new ArrayList();


    public InternalServiceException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InternalServiceException(String message, int errorCode, String errorMessage, String errorDescription,
                                    List<ErrorDetails> errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.errorDetails = errorDetails;
    }


    public InternalServiceException(int errorCode, String errorMessage, String errorDescription) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public List<ErrorDetails> getErrorDetails() {
        return errorDetails;
    }

    public static class ErrorDetails {

        private final String errorField;
        private final String errorKey;

        public ErrorDetails(String errorField, String errorKey) {
            this.errorField = errorField;
            this.errorKey = errorKey;
        }

        public String getErrorField() {
            return errorField;
        }

        public String getErrorKey() {
            return errorKey;
        }
    }
}
