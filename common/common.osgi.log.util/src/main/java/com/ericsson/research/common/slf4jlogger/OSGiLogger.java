/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.research.common.slf4jlogger;

import java.util.Date;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * {@inheritDoc}
 */
public class OSGiLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;

    private static String createMessagePart(int logLevel, StackTraceElement stackTraceElement, String message) {
        if (logLevel == LogService.LOG_INFO) {
            return message;
        }
        if (stackTraceElement != null) {
            return message + " ;; (" + "{" + Thread.currentThread().getName() + "} " + stackTraceElement.getClassName() + "."
                    + stackTraceElement.getMethodName() + "#"
                    + stackTraceElement.getLineNumber() + ") ";
        } else {
            return message;
        }
    }

    private static String getLogLevelString(int logLevel) {
        switch (logLevel) {
            case LogService.LOG_ERROR:
                return "ERROR";
            case LogService.LOG_WARNING:
                return "WARNING";
            case LogService.LOG_DEBUG:
                return "DEBUG";
            case LogService.LOG_INFO:
                return "INFO";
            default:
                return "UNKOWN";
        }
    }
    private final boolean detailed;

    {
        detailed = Boolean.getBoolean("com.ericsson.osgilogger.detailed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String msg) {
        internalLog(LogService.LOG_DEBUG, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String format, Object arg) {
        String msgStr = MessageFormatter.format(format, arg).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String format, Object[] argArray) {
        String msgStr = MessageFormatter.arrayFormat(format, argArray).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String msg, Throwable t) {
        internalLog(LogService.LOG_DEBUG, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String msg) {
        internalLog(LogService.LOG_DEBUG, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object arg) {
        String msgStr = MessageFormatter.format(format, arg).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object[] argArray) {
        String msgStr = MessageFormatter.arrayFormat(format, argArray).getMessage();
        internalLog(LogService.LOG_DEBUG, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String msg, Throwable t) {
        internalLog(LogService.LOG_DEBUG, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String msg) {
        internalLog(LogService.LOG_INFO, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object arg) {
        String msgStr = MessageFormatter.format(format, arg).getMessage();
        internalLog(LogService.LOG_INFO, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
        internalLog(LogService.LOG_INFO, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object[] argArray) {
        String msgStr = MessageFormatter.arrayFormat(format, argArray).getMessage();
        internalLog(LogService.LOG_INFO, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String msg, Throwable t) {
        internalLog(LogService.LOG_INFO, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String msg) {
        internalLog(LogService.LOG_WARNING, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object arg) {
        String msgStr = MessageFormatter.format(format, arg).getMessage();
        internalLog(LogService.LOG_WARNING, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
        internalLog(LogService.LOG_WARNING, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object[] argArray) {
        String msgStr = MessageFormatter.arrayFormat(format, argArray).getMessage();
        internalLog(LogService.LOG_WARNING, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String msg, Throwable t) {
        internalLog(LogService.LOG_WARNING, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String msg) {
        internalLog(LogService.LOG_ERROR, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object arg) {
        String msgStr = MessageFormatter.format(format, arg).getMessage();
        internalLog(LogService.LOG_ERROR, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        String msgStr = MessageFormatter.format(format, arg1, arg2).getMessage();
        internalLog(LogService.LOG_ERROR, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object[] argArray) {
        String msgStr = MessageFormatter.arrayFormat(format, argArray).getMessage();
        internalLog(LogService.LOG_ERROR, msgStr, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String msg, Throwable t) {
        internalLog(LogService.LOG_ERROR, msg, t);
    }

    /**
     * Check the availability of the OSGI logging service, and use it is
     * available. Does nothing otherwise.
     *
     * @param level
     * @param message
     * @param t
     */
    private void internalLog(int level, Object message, Throwable t) {
        LogService logservice = OSGILogFactory.getLogService();
        ServiceReference serviceref = OSGILogFactory.getServiceReference();

        StackTraceElement callerInfo = null;
        if (detailed) {
            callerInfo = Thread.currentThread().getStackTrace()[2];
        }
        if (logservice != null) {
            try {
                if (t != null) {
                    logservice.log(serviceref, level, createMessagePart(level, callerInfo, message + ""), t);
                } else {
                    logservice.log(serviceref, level, createMessagePart(level, callerInfo, message + ""));
                }
            } catch (Exception exc) {
                // Service may have become invalid, just ignore any error
                // until the log service reference is updated by the
                // log factory.
            }
        } else {
            BundleContext bundleContext = OSGILogFactory.getContext();
            Bundle bundle;
            try {
                bundle = bundleContext.getBundle();
            } catch (Throwable t1) {
                bundle = null;
            }

            System.out.println(
                    new Date() + " " + getLogLevelString(level)
                    + (bundle != null ? "  #" + bundle.getBundleId() + " " : "")
                    + (bundle != null ? bundle.getSymbolicName() + " " : " ")
                    + createMessagePart(level, callerInfo, message + ""));
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

}
