package com.vinecom.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ndn on 7/20/2015.
 */
public interface Loggable {
    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
