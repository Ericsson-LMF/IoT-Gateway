
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
import com.ericsson.deviceaccess.api.genericdevice.GDAction;
import com.ericsson.deviceaccess.api.genericdevice.GDActionContext;
import com.ericsson.deviceaccess.api.genericdevice.GDActionResult;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity;
import com.ericsson.deviceaccess.spi.impl.MetadataUtil;
import com.ericsson.research.commonutil.StringUtil;
import java.util.HashMap;
import java.util.Map;

public class GDActionImpl extends GDAction.Stub implements GDAction {

    private String path;
    protected String name;
    private final Map<String, GDPropertyMetadata> argumentsMetadata;
    private final Map<String, GDPropertyMetadata> resultMetadata;

    /**
     * Creates action with metadata for arguments and result.
     *
     * @param name
     * @param argumentsMetadata a Map name:String ->
     * metadata:{@link GDPropertyMetadata}
     * @param resultMetadata a Map name:String ->
     * metadata:{@link GDPropertyMetadata}
     */
    public GDActionImpl(String name, Iterable<? extends GDPropertyMetadata> argumentsMetadata, Iterable<? extends GDPropertyMetadata> resultMetadata) {
        this.name = name;

        this.resultMetadata = new HashMap<>();
        if (resultMetadata != null) {
            resultMetadata.forEach(metadata -> this.resultMetadata.put(metadata.getName(), metadata));
        }

        this.argumentsMetadata = new HashMap<>();
        if (argumentsMetadata != null) {
            argumentsMetadata.forEach(metadata -> this.argumentsMetadata.put(metadata.getName(), metadata));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(GDActionContext sac)
            throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.EXECUTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GDActionResult execute(GDProperties arguments)
            throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.EXECUTE);

        GDActionContextImpl context = new GDActionContextImpl(getVerifiedArguments(arguments), createResult());
        execute(context);
        return context.getResult();
    }

    private GDProperties getVerifiedArguments(GDProperties input) {
        GDProperties output = createArguments();
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
    public GDPropertyMetadata[] getResultMetadata() {
        return resultMetadata.values().toArray(
                new GDPropertyMetadata[resultMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GDPropertyMetadata[] getArgumentsMetadata() {
        return argumentsMetadata.values().toArray(
                new GDPropertyMetadata[argumentsMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GDProperties createArguments() {
        return new GDPropertiesImpl(argumentsMetadata.values());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public GDProperties createResult() {
        return new GDPropertiesImpl(resultMetadata.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GDActionContext createActionContext() {
        return new GDActionContextImpl(createArguments(), createResult());
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
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        return path + "/action/" + this.getName();
    }

    @Override
    public void updatePath(String path) {
        GDAccessSecurity.checkPermission(getClass(), Type.SET);
        this.path = path;
    }

    @Override
    public String serialize(Format format) throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        if (format.isJson()) {
            return toJsonString(format, 0);
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    @Override
    public String getSerializedNode(String path, Format format) throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        if (path == null) {
            throw new GDException(405, "Path cannot be null");
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
            throw new GDException(404, "No such node found");
        }
    }

    private String toJsonString(Format format, int indent)
            throws GDException {
        String json = "{";
        json += "\"name\":\"" + StringUtil.escapeJSON(getName()) + "\"";
        StringBuffer sb = new StringBuffer(",");
        if (argumentsMetadata != null && argumentsMetadata.size() > 0) {
            sb.append(MetadataUtil.INSTANCE.metadataToJson("", Format.JSON, "arguments", argumentsMetadata.values())).append(',');
        }
        if (resultMetadata != null && resultMetadata.size() > 0) {
            sb.append(MetadataUtil.INSTANCE.metadataToJson("", Format.JSON, "result", resultMetadata.values())).append(',');
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
