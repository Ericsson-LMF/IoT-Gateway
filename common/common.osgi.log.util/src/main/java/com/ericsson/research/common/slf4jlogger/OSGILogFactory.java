/*
 * User: joel
 * Date: 2011-09-12
 * Time: 08:41
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
package com.ericsson.research.common.slf4jlogger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Implementation of {@link ILoggerFactory} over {@link LogService}.
 * <p/>
 * {@inheritDoc}
 */
public class OSGILogFactory implements ILoggerFactory {
    static private OSGiLogger logger = new OSGiLogger();
    private static ServiceReference serviceref = null;
    private static ServiceTracker logServiceTracker;
    private static BundleContext context;

    public static void initOSGI(BundleContext context) {
        initOSGI(context, null);
    }

    public static void initOSGI(BundleContext context, ServiceReference servref) {
        OSGILogFactory.context = context;
        serviceref = servref;
        if (context != null) {
            logServiceTracker = new ServiceTracker(context, LogService.class.getName(), null);
            logServiceTracker.open();
        }
    }


    static LogService getLogService() {
        if (logServiceTracker != null) {
            return (LogService) logServiceTracker.getService();
        }
        return null;
    }

    static ServiceReference getServiceReference() {
        return serviceref;
    }

    static BundleContext getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    public Logger getLogger(String name) {
        return logger;
    }
}
