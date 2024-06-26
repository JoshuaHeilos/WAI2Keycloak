package com.example.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtil {

    private static boolean enableLogging;

    @Value("${app.enableRequestLogging}")
    public void setEnableLogging(boolean enableLogging) {
        LoggerUtil.enableLogging = enableLogging;
    }

    public static void log(String message) {
        if (enableLogging) {
            System.out.println(message);
        }
    }
}
