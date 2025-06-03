package com.easyliveline.streamingbackend.util;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}