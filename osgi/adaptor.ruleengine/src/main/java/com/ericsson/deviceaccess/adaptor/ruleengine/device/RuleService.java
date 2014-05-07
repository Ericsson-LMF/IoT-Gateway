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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import com.ericsson.deviceaccess.adaptor.ruleengine.device.ConfigurationManager.ConfigurationManagerListener;
import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.schema.ActionDefinition;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedServiceBase;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;

public class RuleService extends SchemaBasedServiceBase implements ConfigurationManagerListener {
	private BundleContext context;
	private HashMap ruleProperties = new HashMap();
	private HashMap rules = new HashMap();
	private HashMap ruleMap = new HashMap();
	private ConfigurationManager cm;
    
	// Define schema for this device
    private static ActionSchema SET_RULE_ACTION = new ActionSchema.Builder("setRule").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("name").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("conditions").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("startTime").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("stopTime").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("weekDays").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("actionsThen").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("actionsElse").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("actionsStart").
                setType(String.class).
                build()).
            addArgumentSchema(new ParameterSchema.Builder("actionsStop").
                setType(String.class).
                build()).
            build();
    private static ActionSchema UNSET_RULE_ACTION = new ActionSchema.Builder("unsetRule").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            build();
    private static ActionSchema INVOKE_RULE_ACTION_THEN = new ActionSchema.Builder("invokeThen").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            build();
    private static ActionSchema INVOKE_RULE_ACTION_ELSE = new ActionSchema.Builder("invokeElse").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            build();
    private static ActionSchema INVOKE_RULE_ACTION_START = new ActionSchema.Builder("invokeStart").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            build();
    private static ActionSchema INVOKE_RULE_ACTION_STOP = new ActionSchema.Builder("invokeStop").
            addArgumentSchema(new ParameterSchema.Builder("id").
                setType(String.class).
                build()).
            build();
    private static ServiceSchema SERVICE_SCHEMA = new ServiceSchema.Builder("RuleService").
            addActionSchema(SET_RULE_ACTION).
            addActionSchema(UNSET_RULE_ACTION).
            addActionSchema(INVOKE_RULE_ACTION_THEN).
            addActionSchema(INVOKE_RULE_ACTION_ELSE).
            addActionSchema(INVOKE_RULE_ACTION_START).
            addActionSchema(INVOKE_RULE_ACTION_STOP).
            build();

	public RuleService() {
        super(SERVICE_SCHEMA);

        // Define the actions
        defineAction("setRule", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                String name = context.getArguments().getStringValue("name");
                String conditions = context.getArguments().getStringValue("conditions");
                String startTime = context.getArguments().getStringValue("startTime");
                String stopTime = context.getArguments().getStringValue("stopTime");
                String weekDays = context.getArguments().getStringValue("weekDays");
                String actionsThen = context.getArguments().getStringValue("actionsThen");
                String actionsElse = context.getArguments().getStringValue("actionsElse");
                String actionsStart = context.getArguments().getStringValue("actionsStart");
                String actionsStop = context.getArguments().getStringValue("actionsStop");
                System.out.println("setRule called");
                
    			try {
					setRule(id, name, conditions, startTime, stopTime, weekDays, actionsThen, actionsElse, actionsStart, actionsStop);
				} catch (Exception e) {
					throw new GenericDeviceException("Invalid rule: " + e.getMessage(), e);
				}
            }
        });
        defineAction("unsetRule", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                System.out.println("unsetRule called");
        		
        		unsetRule(id);
            }
        });
        defineAction("invokeThen", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                Rule rule = (Rule) rules.get(id);
                rule.invokeThen();
            }
        });
        defineAction("invokeElse", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                Rule rule = (Rule) rules.get(id);
                rule.invokeElse();
            }
        });
        defineAction("invokeStart", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                Rule rule = (Rule) rules.get(id);
                rule.invokeStart();
            }
        });
        defineAction("invokeStop", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String id = context.getArguments().getStringValue("id");
                Rule rule = (Rule) rules.get(id);
                rule.invokeStop();
            }
        });
	}
	
	public void start(BundleContext context, ConfigurationManager configurationManager) {
        this.context = context;
		this.cm = configurationManager;
		this.context = context;
		
		cm.registerListener(this);
	}
	
	public void stop() {
		for (Iterator i = rules.values().iterator(); i.hasNext(); ) {
			Rule rule = (Rule) i.next();
			rule.stop();
		}
		
		cm.unregisterListener(this);
	}

	public void setRule(String id, String name, String conditions, String startTime, String stopTime, String weekDays, String actionsThen, String actionsElse, String actionsStart, String actionsStop) throws Exception {
		Rule rule = (Rule) rules.get(id);
		
		if (rule == null) {
			rule = new Rule(id, name, conditions, startTime, stopTime, weekDays, actionsThen, actionsElse, actionsStart, actionsStop);
			rules.put(id, rule);
			rule.start();
		} else {
			rule.setStartTime(startTime);
			rule.setStopTime(stopTime);
			rule.setWeekDays(weekDays);
			rule.setActionsThen(actionsThen);
			rule.setActionsElse(actionsElse);
			rule.setActionsStop(actionsStop);
			rule.setActionsStart(actionsStart);
			rule.setConditions(conditions);
			
			// Remove all mappings from the filter attribute names to this rule
			LinkedList names = rule.getFilterAttributeNames();
			for (Iterator i = names.iterator(); i.hasNext(); ) {
				String attrName = (String) i.next();
				LinkedList ruleList = (LinkedList) ruleMap.get(attrName);
				if (ruleList != null)
					ruleList.remove(rule);
			}
			
			rule.stop();
			rule.start();
		}
		
		LinkedList names = rule.getFilterAttributeNames();
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			String attrName = (String) i.next();
			
			LinkedList ruleList = (LinkedList) ruleMap.get(attrName);
			if (ruleList == null) {
				ruleList = new LinkedList();
				ruleMap.put(attrName, ruleList);
			}
			ruleList.add(rule);
		}

        ParameterSchema ruleProperty = (ParameterSchema) ruleProperties.get(id);
        if (ruleProperty == null) {
        	ruleProperty = new ParameterSchema.Builder(id).setType(String.class).setDefaultValue("").build();
			addDynamicProperty(ruleProperty);
			ruleProperties.put(id, ruleProperty);
        }
        
        try {
            JSONObject json = new JSONObject();
			json.put("name", name);
			json.put("conditions", conditions);
			json.put("startTime", startTime);
			json.put("stopTime", stopTime);
			json.put("weekDays", weekDays);
			json.put("actionsThen", actionsThen);
			json.put("actionsElse", actionsElse);
			json.put("actionsStart", actionsStart);
			json.put("actionsStop", actionsStop);

			String value = json.toString().replace('\r', '\n').replace('\n', ' ');
			getProperties().setStringValue(id, value);
	        cm.setParameter(id, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void unsetRule(String id) {
		Rule rule = (Rule) rules.remove(id);
		if (rule != null)
			rule.stop();
		
        // Remove the property from the service
        ParameterSchema ruleProperty = (ParameterSchema) ruleProperties.get(id);
		removeDynamicProperty(ruleProperty);
		ruleProperties.remove(id);
		
		// Remove all mappings from the filter attribute names to this rule
		LinkedList names = rule.getFilterAttributeNames();
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			String attrName = (String) i.next();
			LinkedList ruleList = (LinkedList) ruleMap.get(attrName);
			if (ruleList != null)
				ruleList.remove(rule);
		}
        
		cm.unsetParameter(id);
	}

	public void handlePropertyUpdate(Dictionary properties, String deviceId, String serviceName, String propertyName) {
		// Only call rules that have an attribute with the same deviceId.serviceName.propertyName
		LinkedList ruleList = (LinkedList) ruleMap.get(deviceId + "." + serviceName + "." + propertyName);
		if (ruleList != null) {
			for (Iterator i = ruleList.iterator(); i.hasNext();) {
				Rule rule = (Rule) i.next();
				if (rule.match(properties)) {
					rule.invokeThen();
				} else {
					rule.invokeElse();
				}
			}
		}
	}

	// Updated configuration
	public void updated(Dictionary added, Dictionary removed, Dictionary modified) {
		// TODO: Handle removed properties
		for (Enumeration e = removed.keys(); e.hasMoreElements();) {
			String id = (String) e.nextElement();
		}
		
		// TODO: Handle modified properties
		for (Enumeration e = modified.keys(); e.hasMoreElements();) {
			String id = (String) e.nextElement();
		}

		// Handle new properties (primarily at startup)
		for (Enumeration e = added.keys(); e.hasMoreElements();) {
			String id = (String) e.nextElement();
			String value = (String) added.get(id);
			
			try {
				JSONObject json = new JSONObject(value);
				String name = json.getString("name");
				String conditions = json.getString("conditions");
				String startTime = json.getString("startTime");
				String stopTime = json.getString("stopTime");
				String weekDays = json.getString("weekDays");
				String actionsThen = json.getString("actionsThen");
				String actionsElse = json.getString("actionsElse");
				String actionsStart = json.getString("actionsStart");
				String actionsStop = json.getString("actionsStop");
				setRule(id, name, conditions, startTime, stopTime, weekDays, actionsThen, actionsElse, actionsStart, actionsStop);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		// TODO: Handle change of listeners from other source than internally in this bundle
	}
}
