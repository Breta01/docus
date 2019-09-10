package com.bretahajek.scannerapp.utils;

public interface AsyncResponse<T> {
    void processFinish(T output);
}