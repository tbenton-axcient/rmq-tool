package com.cloudfinder.rmqtool;

import org.springframework.util.ErrorHandler;

public class LoggingErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        t.printStackTrace();
    }
}
