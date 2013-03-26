/*
 * User: joel
 * Date: 2011-09-12
 * Time: 08:36
 *
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.slf4j.impl;

import com.ericsson.research.common.slf4jlogger.OSGILogFactory;
import org.slf4j.ILoggerFactory;

/**
 * This is a static binder for SLF4J. It must have this name and package, so that SLF4J can pick it up.
 */
public class StaticLoggerBinder {
    private static OSGILogFactory factory = new OSGILogFactory();

    /**
     * The unique instance of this class.
     *
     * @deprecated Please use the {@link #getSingleton()} method instead of
     *             accessing this field directly. In future versions, this field
     *             will become private.
     */
    public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * Return the singleton of this class.
     *
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Declare the version of the SLF4J API this implementation is compiled against.
     * The value of this field is usually modified with each release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.6.2";  // !final

    private StaticLoggerBinder() {
    }

    public ILoggerFactory getLoggerFactory() {
        return factory;
    }

    public String getLoggerFactoryClassStr() {
        return factory.getClass().getName();
    }
}
