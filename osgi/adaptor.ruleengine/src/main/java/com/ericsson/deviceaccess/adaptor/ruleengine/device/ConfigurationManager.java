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

import com.ericsson.common.util.LegacyUtil;
import com.ericsson.common.util.function.FunctionalUtil;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, Object> configProperties = new ConcurrentHashMap<>();
    private ServiceRegistration serviceReg;
    private final Queue<ConfigurationManagerListener> listeners = new ConcurrentLinkedQueue<>();

    public ConfigurationManager(BundleContext context, String pid) {
        this.context = context;
        this.pid = pid;
    }

    public void start() {
        Map<String, Object> properties = new ConcurrentHashMap<>();
        properties.put(Constants.SERVICE_PID, pid);
        serviceReg = context.registerService(ManagedService.class, this, LegacyUtil.toDictionary(properties));
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
        configProperties.computeIfPresent(key, (k, v) -> {
            updateConfig(configProperties);
            return null;
        });
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) {
            return;
        }
        Map<String, Object> props = LegacyUtil.toMap(properties);
        props.remove(Constants.SERVICE_PID);

        // Check for added configuration parameters
        Map<String, Object> added = new HashMap<>();
        props.forEach((key, v) -> {
            configProperties.computeIfAbsent(key, k -> {
                added.put(k, v);
                return v;
            });
        });

        // Check for removed configuration parameters
        Map<String, Object> removed = configProperties
                .entrySet()
                .stream()
                .filter(e -> !props.containsKey(e.getKey()))
                .collect(FunctionalUtil.entryCollector());
        removed.keySet().forEach(configProperties::remove);

        // Check for modified configuration parameters
        Map<String, Object> modified = configProperties
                .entrySet()
                .stream()
                .filter(e -> !props.get(e.getKey()).equals(e.getValue()))
                .peek(e -> {
                    e.setValue(props.get(e.getKey()));
                })
                .collect(FunctionalUtil.entryCollector());

        if (!added.isEmpty() || !removed.isEmpty() || !modified.isEmpty()) {
            listeners.forEach(l -> l.updated(added, removed, modified));
        }
    }

    private void updateConfig(Map<String, Object> updatedConfig) {
        // Assume contest holds a valid BundleContext object for the bundle
        ServiceReference ref = context.getServiceReference(ConfigurationAdmin.class.getName());
        if (ref != null) {
            ConfigurationAdmin cfgAdm = (ConfigurationAdmin) context.getService(ref);
            try {
                Configuration config = cfgAdm.getConfiguration(pid);
                config.update(LegacyUtil.toDictionary(updatedConfig));
            } catch (IOException e) {
            }
        } else {
            System.err.println("ConfigurationAdmin is not found");
        }
    }

    public interface ConfigurationManagerListener {

        void updated(Map<String, Object> added, Map<String, Object> removed, Map<String, Object> modified);
    }
}
