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

import com.ericsson.common.util.serialization.Format;
import com.ericsson.common.util.serialization.SerializationException;
import com.ericsson.common.util.serialization.SerializationUtil;
import com.ericsson.deviceaccess.adaptor.ruleengine.device.ConfigurationManager.ConfigurationManagerListener;
import com.ericsson.deviceaccess.adaptor.ruleengine.device.Rule.RuleInfo;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.deviceaccess.spi.schema.based.SBServiceBase;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class RuleService extends SBServiceBase implements ConfigurationManagerListener {

    // Define schema for this device
    private static final ActionSchema SET_RULE_ACTION = new ActionSchema.Builder().setName("setRule")
            .addArgument("id", String.class)
            .addArgument("name", String.class)
            .addArgument("conditions", String.class)
            .addArgument("startTime", String.class)
            .addArgument("stopTime", String.class)
            .addArgument("weekDays", String.class)
            .addArgument("actionsThen", String.class)
            .addArgument("actionsElse", String.class)
            .addArgument("actionsStart", String.class)
            .addArgument("actionsStop", String.class)
            .build();
    private static final ActionSchema UNSET_RULE_ACTION = new ActionSchema.Builder().setName("unsetRule")
            .addArgument("id", String.class).build();
    private static final ActionSchema INVOKE_RULE_ACTION_THEN = new ActionSchema.Builder().setName("invokeThen")
            .addArgument("id", String.class).build();
    private static final ActionSchema INVOKE_RULE_ACTION_ELSE = new ActionSchema.Builder().setName("invokeElse")
            .addArgument("id", String.class).build();
    private static final ActionSchema INVOKE_RULE_ACTION_START = new ActionSchema.Builder().setName("invokeStart")
            .addArgument("id", String.class).build();
    private static final ActionSchema INVOKE_RULE_ACTION_STOP = new ActionSchema.Builder().setName("invokeStop").
            addArgument("id", String.class).build();
    private static final ServiceSchema SERVICE_SCHEMA = new ServiceSchema.Builder().setName("RuleService")
            .addAction(SET_RULE_ACTION)
            .addAction(UNSET_RULE_ACTION)
            .addAction(INVOKE_RULE_ACTION_THEN)
            .addAction(INVOKE_RULE_ACTION_ELSE)
            .addAction(INVOKE_RULE_ACTION_START)
            .addAction(INVOKE_RULE_ACTION_STOP)
            .build();
    private BundleContext context;
    private final HashMap<String, ParameterSchema> ruleProperties = new HashMap<>();
    private HashMap<String, Rule> rules = new HashMap<>();
    private final HashMap<String, List<Rule>> ruleMap = new HashMap<>();
    private ConfigurationManager configManager;

    public RuleService() {
        super(SERVICE_SCHEMA);

        // Define the actions
        defineAction("setRule", actionContext -> {
            GDProperties args = actionContext.getArguments();
            String id = args.getStringValue("id");
            System.out.println("setRule called");

            try {
                setRule(id, new RuleInfo(args));
            } catch (SerializationException e) {
                throw new GDException("Invalid rule: " + e.getMessage(), e);
            }
        });
        defineAction("unsetRule", actionContext -> {
            String id = actionContext.getArguments().getStringValue("id");
            System.out.println("unsetRule called");
            unsetRule(id);
        });
        defineAction("invokeThen", actionContext -> {
            String id = actionContext.getArguments().getStringValue("id");
            rules.get(id).invokeThen();
        });
        defineAction("invokeElse", actionContext -> {
            String id = actionContext.getArguments().getStringValue("id");
            rules.get(id).invokeElse();
        });
        defineAction("invokeStart", actionContext -> {
            String id = actionContext.getArguments().getStringValue("id");
            rules.get(id).invokeStart();
        });
        defineAction("invokeStop", actionContext -> {
            String id = actionContext.getArguments().getStringValue("id");
            rules.get(id).invokeStop();
        });
    }

    public void start(BundleContext context, ConfigurationManager configurationManager) {
        this.context = context;
        this.configManager = configurationManager;
        this.context = context;

        configManager.registerListener(this);
    }

    public void stop() {
        rules.values().forEach(r -> r.stop());
        configManager.unregisterListener(this);
    }

    public final void setRule(String id, RuleInfo info) throws SerializationException {
        Rule rule = rules.compute(id, (key, value) -> {
            if (value == null) {
                return new Rule(key, info).start();
            }
            value.setStartTime(info.startTime);
            value.setStopTime(info.stopTime);
            value.setWeekDays(info.weekDays);
            value.setActionsThen(info.actionsThen);
            value.setActionsElse(info.actionsElse);
            value.setActionsStop(info.actionsStop);
            value.setActionsStart(info.actionsStart);
            try {
                value.setConditions(info.conditions);
            } catch (InvalidSyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }

            // Remove all mappings from the filter attribute names to this value
            value.getFilterAttributeNames().stream()
                    .map(attrName -> ruleMap.getOrDefault(attrName, Collections.emptyList()))
                    .forEach(ruleList -> ruleList.remove(value));
            value.stop();
            value.start();
            return value;
        });

        rule.getFilterAttributeNames().forEach(attrName -> {
            ruleMap.computeIfAbsent(attrName, key -> new LinkedList()).add(rule);
        });

        ruleProperties.computeIfAbsent(id, key -> {
            ParameterSchema ruleProperty = new ParameterSchema.Builder(key, String.class).build();
            addDynamicProperty(ruleProperty);
            return ruleProperty;
        });

        configManager.setParameter(id, SerializationUtil.execute(Format.JSON, m -> m.writer().writeValueAsString(info)));
    }

    public final void unsetRule(String id) {
        Rule rule = rules.remove(id);
        if (rule != null) {
            rule.stop();
        }

        // Remove the property from the service
        removeDynamicProperty(ruleProperties.get(id));
        ruleProperties.remove(id);

        // Remove all mappings from the filter attribute names to this rule
        if (rule != null) {
            rule.getFilterAttributeNames()
                    .stream()
                    .map(attrName -> ruleMap.getOrDefault(attrName, Collections.emptyList()))
                    .forEach(ruleList -> ruleList.remove(rule));
        }
        configManager.unsetParameter(id);
    }

    public void handlePropertyUpdate(Map<String, Object> properties, String deviceId, String serviceName, String propertyName) {
        // Only call rules that have an attribute with the same deviceId.serviceName.propertyName
        String ruleName = deviceId + "." + serviceName + "." + propertyName;
        ruleMap.getOrDefault(ruleName, Collections.emptyList())
                .forEach(rule -> {
                    if (rule.match(properties)) {
                        rule.invokeThen();
                    } else {
                        rule.invokeElse();
                    }
                });
    }

    // Updated configuration
    @Override
    public void updated(Map<String, Object> added, Map<String, Object> removed, Map<String, Object> modified) {
        // TODO: Handle removed properties
        removed.forEach((k, v) -> {
        });

        // TODO: Handle modified properties
        modified.forEach((k, v) -> {
        });

        // Handle new properties (primarily at startup)
        modified.forEach((key, value) -> {
            try {
                setRule(key, SerializationUtil.execute(Format.JSON, m -> m.reader().readValue((String) value)));
            } catch (SerializationException ex) {
                Logger.getLogger(RuleService.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
