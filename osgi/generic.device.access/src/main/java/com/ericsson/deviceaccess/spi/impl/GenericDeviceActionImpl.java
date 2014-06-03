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

import com.ericsson.deviceaccess.api.GenericDeviceAction;
import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceActionResult;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.research.commonutil.StringUtil;
import java.util.HashMap;
import java.util.Map;

public class GenericDeviceActionImpl extends GenericDeviceAction.Stub implements GenericDeviceAction {

    private String path;
    protected String name;
    private Map argumentsMetadata;
    private Map resultMetadata;

    /**
     * Creates action with metadata for arguments and result.
     *
     * @param name
     * @param argumentsMetadata a Map name:String ->
     * metadata:{@link GenericDevicePropertyMetadata}
     * @param resultMetadata a Map name:String ->
     * metadata:{@link GenericDevicePropertyMetadata}
     */
    protected GenericDeviceActionImpl(String name, GenericDevicePropertyMetadata[] argumentsMetadata, GenericDevicePropertyMetadata[] resultMetadata) {
        this.name = name;

        this.resultMetadata = new HashMap();
        if (resultMetadata != null) {
            for (GenericDevicePropertyMetadata metadata : resultMetadata) {
                this.resultMetadata.put(metadata.getName(), metadata);
            }
        }

        this.argumentsMetadata = new HashMap();
        if (argumentsMetadata != null) {
            for (GenericDevicePropertyMetadata metadata : argumentsMetadata) {
                this.argumentsMetadata.put(metadata.getName(), metadata);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(GenericDeviceActionContext sac)
            throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkExecutePermission(getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GenericDeviceActionResult execute(GenericDeviceProperties arguments)
            throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkExecutePermission(getClass().getName());

        GenericDeviceActionContextImpl context = new GenericDeviceActionContextImpl(getVerifiedArguments(arguments), createResult());
        execute(context);
        return context.getResult();
    }

    private GenericDeviceProperties getVerifiedArguments(
            GenericDeviceProperties input) {

        GenericDeviceProperties output = createArguments();
        if (input == null) {
            return output;
        }

        String keys[] = output.getNames();
        for (String key : keys) {
            if (!input.hasProperty(key)) {
                continue;
            }
            Object value = input.getValue(key);
            if (value instanceof String) {
                output.setStringValue(key, (String) value);
            } else if (value instanceof Integer) {
                output.setIntValue(key, ((Integer) value));
            } else if (value instanceof Float) {
                output.setFloatValue(key, ((Float) value));
            } else if (value instanceof Long) {
                output.setLongValue(key, ((Long) value));
            }
        }

        MetadataUtil.INSTANCE.verifyPropertiesAgainstMetadata(output, argumentsMetadata);

        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDevicePropertyMetadata[] getResultMetadata() {
        return (GenericDevicePropertyMetadata[]) resultMetadata.values().toArray(
                new GenericDevicePropertyMetadata[resultMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDevicePropertyMetadata[] getArgumentsMetadata() {
        return (GenericDevicePropertyMetadata[]) argumentsMetadata.values().toArray(
                new GenericDevicePropertyMetadata[argumentsMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDeviceProperties createArguments() {
        return new GenericDevicePropertiesImpl(
                (GenericDevicePropertyMetadata[]) argumentsMetadata.values().toArray(
                        new GenericDevicePropertyMetadata[argumentsMetadata.size()]));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public GenericDeviceProperties createResult() {
        return new GenericDevicePropertiesImpl(
                (GenericDevicePropertyMetadata[]) resultMetadata.values().toArray(
                        new GenericDevicePropertyMetadata[resultMetadata.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDeviceActionContext createActionContext() {
        return new GenericDeviceActionContextImpl(createArguments(), createResult());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(boolean isAbsolute) {
        return path + "/action/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/action/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(int format) throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            return toJsonString(format, 0);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializedNode(String path, int format)
            throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (path == null) {
            throw new GenericDeviceException(405, "Path cannot be null");
        }

        if (path.length() == 0) {
            return serialize(format);
        } else if (path.equals("name")) {
            return getName();
        } else if (path.startsWith("arguments") && argumentsMetadata.size() > 0) {
            return MetadataUtil.INSTANCE.metadataToJson(path, format, "arguments", argumentsMetadata.values());
        } else if (path.startsWith("result") && resultMetadata.size() > 0) {
            return MetadataUtil.INSTANCE.metadataToJson(path, format, "result", resultMetadata.values());
        } else {
            throw new GenericDeviceException(404, "No such node found");
        }
    }

    private String toJsonString(int format, int indent)
            throws GenericDeviceException {
        String json = "{";
        json += "\"name\":\"" + StringUtil.escapeJSON(getName()) + "\"";
        StringBuffer sb = new StringBuffer(",");
        if (argumentsMetadata != null && argumentsMetadata.size() > 0) {
            sb.append(MetadataUtil.INSTANCE.metadataToJson("", Serializable.FORMAT_JSON, "arguments", argumentsMetadata.values())).append(',');
        }
        if (resultMetadata != null && resultMetadata.size() > 0) {
            sb.append(MetadataUtil.INSTANCE.metadataToJson("", Serializable.FORMAT_JSON, "result", resultMetadata.values())).append(',');
        }
        if (sb.length() > 1) {
            // Remove last ','
            sb.setLength(sb.length() - 1);
            json += sb;
        }
        json += "}";
        return json;
    }
}
