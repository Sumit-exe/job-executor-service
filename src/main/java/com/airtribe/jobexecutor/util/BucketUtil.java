package com.airtribe.jobexecutor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BucketUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    public static String getCurrentBucket() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static String getBucket(LocalDateTime time) {
        return time.format(FORMATTER);
    }
}