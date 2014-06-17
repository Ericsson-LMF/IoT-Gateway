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
package com.ericsson.deviceaccess.spi.impl.genericdevice;

import com.ericsson.deviceaccess.api.genericdevice.GDAccessPermission.Type;
import com.ericsson.deviceaccess.api.genericdevice.GDActionContext;
import com.ericsson.deviceaccess.api.genericdevice.GDActionResult;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity;

public class GDActionContextImpl extends GDActionContext.Stub implements GDActionContext {

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
    private final GDProperties arguments;
    private GDActionResult result;

    /**
     * @param arguments
     * @param result
     */
    public GDActionContextImpl(GDProperties arguments, GDProperties result) {
        super();
        this.arguments = arguments;
        this.result = new GDActionResultImpl(result);
    }

    @Override
    public void setRequester(String requester) {
        this.requester = requester;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    @Override
    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String getDevice() {
        return device;
    }

    @Override
    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    @Override
    public boolean isAuthorized() {
        if (getRequester() != null && getRequester().equals(getOwner())) {
            return true;
        } else {
            return this.isAuthorized;
        }
    }

    @Override
    public void setFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    @Override
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

    @Override
    public GDActionResult getResult() {
        return result;
    }

    @Override
    public void setRequesterContact(String requesterContact) {
        this.requesterContact = requesterContact;
    }

    @Override
    public String getRequesterContact() {
        return requesterContact;
    }

    @Override
    public GDProperties getArguments() {
        return arguments;
    }

    public void setResult(GDActionResult result) {
        this.result = result;
    }

    @Override
    public String serialize(Format format) throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        if (format.isJson()) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"device\":\"").append(getDevice()).append("\",");
            sb.append("\"service\":\"").append(getService()).append("\",");
            sb.append("\"action\":\"").append(getAction()).append("\",");
            sb.append("\"requester\":\"").append(getRequester()).append("\",");
            sb.append("\"owner\":\"").append(getOwner()).append("\",");
            sb.append("\"firstTime\":\"").append(isFirstTime()).append("\",");
            sb.append("\"authorized\":\"").append(isAuthorized()).append("\",");
            sb.append("\"requesterContact\":\"").append(getRequesterContact()).append("\",");
            if (arguments != null) {
                sb.append("\"arguments\":").append(getArguments().serialize(format)).append(",");
            } else {
                sb.append("\"arguments\":null,");
            }
            if (result != null) {
                sb.append("\"result\":").append(getResult().serialize(format));
            } else {
                sb.append("\"result\":null");
            }
            sb.append("}");

            return sb.toString();
        }
        throw new GDException(405, "No such format supported");
    }

}
