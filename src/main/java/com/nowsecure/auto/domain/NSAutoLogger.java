package com.nowsecure.auto.domain;

/**
 * This interface is used for logging
 * 
 * @author sbhatti
 *
 */
public interface NSAutoLogger {

    void info(String msg);
    
    void info(String msg, Color color);

    void error(String msg);

    void debug(String msg);
}
