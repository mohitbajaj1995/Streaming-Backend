package com.easyliveline.streamingbackend.util;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}