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
package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceActionResult;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;


public class GenericDeviceActionContextImpl extends GenericDeviceActionContext.Stub implements GenericDeviceActionContext {
    private String requester;
    private String requesterContact;
    private String owner;
    private String device;
    private String service;
    private String action;
    private boolean isAuthorized = true;
    private boolean isFirstTime = true;
    private boolean isExecuted = false;
    private boolean isFailed = false;
    private long messageThreadId = this.hashCode();
    private GenericDeviceProperties arguments;
    private GenericDeviceActionResult result;

    /**
     * @param arguments
     * @param result
     */
    public GenericDeviceActionContextImpl(GenericDeviceProperties arguments, GenericDeviceProperties result) {
        super();
        this.arguments = arguments;
        this.result = new GenericDeviceActionResultImpl(result);
    }


    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public boolean isAuthorized() {
        if (getRequester() != null && getRequester().equals(getOwner())) {
            return true;
        } else {
            return this.isAuthorized;
        }
    }


    public void setFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }


    public long getMessageThreadId() {
        return messageThreadId;
    }

    public void setMessageThreadId(long id) {
        this.messageThreadId = id;
    }

    public void setExecuted(boolean isExecuted) {
        this.isExecuted = isExecuted;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public void setFailed(boolean isFailed) {
        this.isFailed = isFailed;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public GenericDeviceActionResult getResult() {
        return result;
    }

    public void setRequesterContact(String requesterContact) {
        this.requesterContact = requesterContact;
    }

    public String getRequesterContact() {
        return requesterContact;
    }

    public GenericDeviceProperties getArguments() {
        return arguments;
    }


	public void setResult(GenericDeviceActionResult result) {
		this.result = result;		
	}


	public String serialize(int format) throws GenericDeviceException {
		GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            StringBuffer sb = new StringBuffer("{");
            sb.append("\"device\":\"").append(getDevice()).append("\",");
            sb.append("\"service\":\"").append(getService()).append("\",");
            sb.append("\"action\":\"").append(getAction()).append("\",");
            sb.append("\"requester\":\"").append(getRequester()).append("\",");
            sb.append("\"owner\":\"").append(getOwner()).append("\",");
            sb.append("\"firstTime\":\"").append(isFirstTime()).append("\",");
            sb.append("\"authorized\":\"").append(isAuthorized()).append("\",");
            sb.append("\"requesterContact\":\"").append(getRequesterContact()).append("\",");
            if(arguments != null){
            	sb.append("\"arguments\":").append(getArguments().serialize(format)).append(",");
            } else {
            	sb.append("\"arguments\":null,");
            }
            if(result != null){
            	sb.append("\"result\":").append(getResult().serialize(format));
            } else {
            	sb.append("\"result\":null");
            }
            sb.append("}");
            
            return sb.toString();
        } 
        throw new GenericDeviceException(405, "No such format supported");
	}


}
