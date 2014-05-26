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
package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class ConfigurationManager implements ManagedService {

    private final BundleContext context;
    private final String pid;
    private final Dictionary configProperties = new Properties();
    private ServiceRegistration serviceReg;
    private final Queue<ConfigurationManagerListener> listeners = new ConcurrentLinkedQueue<>();

    public ConfigurationManager(BundleContext context, String pid) {
        this.context = context;
        this.pid = pid;
    }

    public interface ConfigurationManagerListener {

        public void updated(Dictionary added, Dictionary removed, Dictionary modified);
    }

    public void start() {
        Dictionary properties = new Properties();
        properties.put(Constants.SERVICE_PID, pid);
        serviceReg = context.registerService(ManagedService.class.getName(), this, properties);
    }

    public void stop() {
        serviceReg.unregister();
    }

    public void registerListener(ConfigurationManagerListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(ConfigurationManagerListener listener) {
        listeners.remove(listener);
    }

    public void setParameter(String key, String value) {
        configProperties.put(key, value);
        updateConfig(configProperties);
    }

    public void unsetParameter(String key) {
        if (configProperties.get(key) != null) {
            configProperties.remove(key);
            updateConfig(configProperties);
        }
    }

    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        if (properties == null) {
            return;
        }
        properties.remove(Constants.SERVICE_PID);

        // Check for added configuration parameters
        Properties added = new Properties();
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            Object value = properties.get(key);
            if (configProperties.get(key) == null) {
                added.put(key, value);
                configProperties.put(key, value);
            }
        }

        // Check for removed configuration parameters
        Properties removed = new Properties();
        for (Enumeration e = configProperties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (properties.get(key) == null) {
                removed.put(key, configProperties.get(key));
                configProperties.remove(key);
            }
        }

        // Check for modified configuration parameters
        Properties modified = new Properties();
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String newValue = (String) properties.get(key);
            String oldValue = (String) configProperties.get(key);
            if (!newValue.equals(oldValue)) {
                modified.put(key, newValue);
                configProperties.put(key, newValue);
            }
        }

        if (added.size() > 0 || removed.size() > 0 || modified.size() > 0) {
            listeners.forEach(l -> l.updated(added, removed, modified));
        }
    }

    private void updateConfig(Dictionary updatedConfig) {
        // Assume contest holds a valid BundleContext object for the bundle
        ServiceReference ref = context.getServiceReference(ConfigurationAdmin.class.getName());
        if (ref != null) {
            ConfigurationAdmin cfgAdm = (ConfigurationAdmin) context.getService(ref);
            try {
                Configuration config = cfgAdm.getConfiguration(pid);
                config.update(updatedConfig);
            } catch (IOException e) {
            }
        } else {
            System.err.println("ConfigurationAdmin is not found");
        }
    }
}
