package com.nowsecure.auto.utils;

import java.io.File;
import java.io.IOException;

/**
 * Defines high level APIs to communicate with backend services
 * 
 * @author sbhatti
 *
 */
public interface IOHelperI {
    void save(File file, String contents) throws IOException;

    byte[] load(File file) throws IOException;

    String get(String uri, String apiKey) throws IOException;

    String post(String uri, String apiKey) throws IOException;

    String upload(String uri, String apiKey, File file) throws IOException;

}