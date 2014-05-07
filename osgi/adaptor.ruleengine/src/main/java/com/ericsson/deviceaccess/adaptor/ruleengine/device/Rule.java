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

import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.ericsson.deviceaccess.adaptor.ruleengine.Activator;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceAction;
import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.GenericDeviceService;

public class Rule {
	final static HashMap WEEKDAYS = new HashMap(){{put("sun", new Integer(0));put("mon", new Integer(1));put("tue", new Integer(2));put("wed", new Integer(3));put("thu", new Integer(4));put("fri", new Integer(5));put("sat", new Integer(6));}};
	private String id;
	private String name;
	private String conditions;
	private String startTime;
	private float startTimeFloat;
	private String stopTime;
	private float stopTimeFloat;
	private String weekDays;
	private boolean[] weekDaysBool;
	private LinkedList actionsThen;
	private LinkedList actionsElse;
	private LinkedList actionsStart;
	private LinkedList actionsStop;
	LDAPExpr ldapFilter;
	private Timer timer = null;
	private boolean started = false;
	
	public Rule(String id, String name, String conditions, String startTime, String stopTime, String weekDays, String actionsThen, String actionsElse, String actionsStart, String actionsStop) throws Exception {
		this.id = id;
		this.name = name;
		this.conditions = conditions;
		setStartTime(startTime);
		setStopTime(stopTime);
		setWeekDays(weekDays);
		setActionsThen(actionsThen);
		setActionsElse(actionsElse);
		setActionsStart(actionsStart);
		setActionsStop(actionsStop);
		ldapFilter = new LDAPExpr(conditions);
	}

	public void start() {
		schedule();
		started = true;
	}

	public void stop() {
		started = false;
		
		unschedule();
	}

	public void setConditions(String conditions) throws InvalidSyntaxException {
		this.conditions = conditions;
		
		ldapFilter = new LDAPExpr(conditions);
	}
	
	protected LDAPExpr getLDAPFilter() {
		return ldapFilter;
	}
	
	public String getConditions() {
		return conditions;
	}

	public void setStartTime(String startTime) throws Exception {
		if (startTime.trim().length() == 0) {
			startTime = "";
			return;
		}
		
		int idx = startTime.indexOf(":");
		if (idx<=0)
			throw new Exception("Illegal start time"); // TODO: Allow this if empty
		
		int hours = Integer.parseInt(startTime.substring(0, idx));
		int minutes = Integer.parseInt(startTime.substring(idx+1));
		startTimeFloat = hours + ((float)minutes)/60;

		this.startTime = startTime;
	}
	
	public void setStopTime(String stopTime) throws Exception {
		if (stopTime.trim().length() == 0) {
			stopTime = "";
			return;
		}
		
		int idx = stopTime.indexOf(":");
		if (idx<=0)
			throw new Exception("Illegal stop time");
		
		int hours = Integer.parseInt(stopTime.substring(0, idx));
		int minutes = Integer.parseInt(stopTime.substring(idx+1));
		stopTimeFloat = hours + ((float)minutes)/60;

		this.stopTime = stopTime;
	}
	
	public void setWeekDays(String weekDays) {
		weekDaysBool = new boolean[7];
		
		StringTokenizer st = new StringTokenizer(weekDays, ",");
		while (st.hasMoreElements()) {
			String weekDay = st.nextToken().trim();
			Integer weekDayIdx = (Integer) WEEKDAYS.get(weekDay);
			if (weekDayIdx == null)
				throw new IllegalArgumentException("Invalid weekday: " + weekDay);
			
			weekDaysBool[weekDayIdx.intValue()] = true;
		}
		
		this.weekDays = weekDays;
	}
	
	private LinkedList createRuleActions(String actions) throws JSONException {
		LinkedList actionsList = new LinkedList();
		if (actions.trim().length() == 0)
			return actionsList;
		
		JSONArray json = new JSONArray(actions);
		for (int i = 0; i < json.length(); i++) {
			JSONObject action = json.getJSONObject(i);
			RuleAction ruleAction = new RuleAction();
			ruleAction.setDeviceId(action.getString("id"));
			ruleAction.setServiceName(action.getString("service"));
			ruleAction.setActionName(action.getString("action"));
			JSONArray args = action.getJSONArray("args");
			HashMap arguments = new HashMap();
			for (int j = 0; j < args.length(); j++) {
				JSONObject arg = args.getJSONObject(j);
				String argName = (String) arg.keys().next();
				arguments.put(argName, arg.getString(argName));
			}
			ruleAction.setArguments(arguments);
			actionsList.add(ruleAction);
		}
		
		return actionsList;
	}
	
	public void setActionsThen(String actionsThen) throws JSONException {
		this.actionsThen = createRuleActions(actionsThen);
	}

	public void setActionsElse(String actionsElse) throws JSONException {
		this.actionsElse = createRuleActions(actionsElse);
	}

	public void setActionsStart(String actionsStart) throws JSONException {
		this.actionsStart = createRuleActions(actionsStart);
	}

	public void setActionsStop(String actionsStop) throws JSONException {
		this.actionsStop = createRuleActions(actionsStop);
	}

	public boolean checkTimeOfDay() {
		Calendar date = Calendar.getInstance();
		int hourOfDay = date.get(Calendar.HOUR_OF_DAY);
		int minute = (int)((float)date.get(Calendar.MINUTE));
		float time = hourOfDay + ((float)minute)/60;
		
		if (startTimeFloat < stopTimeFloat) {
			return (time >= startTimeFloat && time < stopTimeFloat);
		} else {
			if (time >= startTimeFloat)
				return true;
			else if (time < stopTimeFloat)
				return true;
		}

		return false;
	}
	
	public boolean checkWeekDay() {
		Calendar date = Calendar.getInstance();
		int day = date.get(Calendar.DAY_OF_WEEK);

		return weekDaysBool[day-1];
	}
	
	private void invokeActions(LinkedList actions) {
		for (Iterator i = actions.iterator(); i.hasNext(); ) {
			RuleAction ruleAction = (RuleAction) i.next();
			GenericDevice device = getDevice(ruleAction.getDeviceId());
			if (device != null) {
				GenericDeviceAction action = device.getService(ruleAction.getServiceName()).getAction(ruleAction.getActionName());
				GenericDeviceProperties args = action.createArguments();
				for (Iterator argIter = ruleAction.getArguments().keySet().iterator(); argIter.hasNext(); ) {
					String argName = (String) argIter.next();
					String argValue = (String) ruleAction.getArguments().get(argName);
					args.setStringValue(argName, argValue);
				}
				
				try {
					action.execute(args);
				} catch (GenericDeviceException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private GenericDevice getDevice(String deviceId) {
		try {
			String idFilter = "(" + Constants.PARAM_DEVICE_ID + "=" + deviceId + ")";
			ServiceReference[] refs = Activator.context.getAllServiceReferences(GenericDevice.class.getName(), idFilter);
			if (refs != null) {
				//logger.debug(refs.length + " device found");
				for (int i = 0; i < refs.length; i++) {
					GenericDevice dev = (GenericDevice) Activator.context.getService(refs[i]);
					if (deviceId.equals(dev.getId())) {
						return dev;
					}
				}
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void invokeThen() {
		if (!checkWeekDay() || !checkTimeOfDay())
			return;

		System.out.println("Invoking Then for rule " + name + ", " + new Date());
		invokeActions(actionsThen);
	}

	public void invokeElse() {
		if (!checkWeekDay() || !checkTimeOfDay())
			return;

		System.out.println("Invoking Else for rule " + name + ", " + new Date());
		invokeActions(actionsElse);
	}

	public void invokeStart() {
		if (!checkWeekDay())
			return;

		System.out.println("Invoking Start for rule " + name + ", " + new Date());
		invokeActions(actionsStart);
	}

	public void invokeStop() {
		// TODO: If stopTime is after midnight we should allow it if the previous weekday is enabled
		if (!checkWeekDay())
			return;

		System.out.println("Invoking Stop for rule " + name + ", " + new Date());
		invokeActions(actionsStop);
	}

	public String getName() {
		return name;
	}
	
	public LinkedList getFilterAttributeNames() {
		return getLDAPFilter().getAttributeNames();
	}
	
	public void schedule() {
		unschedule();
		timer = new Timer();
		
		if (startTime != null && startTime.trim().length() != 0) {
			try {
				int idx = startTime.indexOf(":");
				if (idx<=0)
					throw new Exception("Illegal start time");
				
				int hours = Integer.parseInt(startTime.substring(0, idx));
				int minutes = Integer.parseInt(startTime.substring(idx+1));
				Calendar date = Calendar.getInstance();
				date.set(Calendar.HOUR_OF_DAY, hours);
				date.set(Calendar.MINUTE, minutes);
				date.set(Calendar.SECOND, 1);
				Date triggerDate = date.getTime();
				if (triggerDate.getTime() <= new Date().getTime())
					triggerDate = new Date(triggerDate.getTime() + 24*3600000);
				System.out.println(triggerDate);
				TimerTask task = new TimerTask(){
					public void run() {
						if (ldapFilter.evaluate(PropertyManager.getInstance().getDeviceProperties(), true))
							invokeStart();
					}};
				timer.scheduleAtFixedRate(task, triggerDate, 24*3600000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (stopTime != null && stopTime.trim().length() != 0) {
			try {
				int idx = stopTime.indexOf(":");
				if (idx<=0)
					throw new Exception("Illegal stop time");
				
				int hours = Integer.parseInt(stopTime.substring(0, idx));
				int minutes = Integer.parseInt(stopTime.substring(idx+1));
				Calendar date = Calendar.getInstance();
				date.set(Calendar.HOUR_OF_DAY, hours);
				date.set(Calendar.MINUTE, minutes);
				date.set(Calendar.SECOND, 1);
				Date triggerDate = date.getTime();
				if (triggerDate.getTime() <= new Date().getTime())
					triggerDate = new Date(triggerDate.getTime() + 24*3600000);
				System.out.println(triggerDate);
				TimerTask task = new TimerTask(){
					public void run() {
						if (ldapFilter.evaluate(PropertyManager.getInstance().getDeviceProperties(), true))
							invokeStop();
					}};
				timer.scheduleAtFixedRate(task, triggerDate, 24*3600000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void unschedule() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public boolean match(Dictionary properties) {
		return ldapFilter.evaluate(properties, true);
	}
	
	public String toString() {
		return "Rule: " + name;
	}
}
