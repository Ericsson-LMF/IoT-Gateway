package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.*;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.utility.Utils;

import java.util.HashMap;
import java.util.Map;

public class GenericDeviceActionImpl extends GenericDeviceAction.Stub implements GenericDeviceAction {
    private static MetadataUtil metadataUtil = MetadataUtil.getInstance();
    private String path;
    protected String name;
    private Map argumentsMetadata;
    private Map resultMetadata;

    /**
     * Creates action with metadata for arguments and result.
     *
     * @param name
     * @param argumentsMetadata a Map name:String -> metadata:{@link GenericDevicePropertyMetadata}
     * @param resultMetadata    a Map name:String -> metadata:{@link GenericDevicePropertyMetadata}
     */
    protected GenericDeviceActionImpl(String name, GenericDevicePropertyMetadata[] argumentsMetadata, GenericDevicePropertyMetadata[] resultMetadata) {
        this.name = name;

        this.resultMetadata = new HashMap();
        if (resultMetadata != null) {
            for (int i = 0; i < resultMetadata.length; i++) {
                GenericDevicePropertyMetadata metadata = resultMetadata[i];
                this.resultMetadata.put(metadata.getName(), metadata);
            }
        }

        this.argumentsMetadata = new HashMap();
        if (argumentsMetadata != null) {
            for (int i = 0; i < argumentsMetadata.length; i++) {
                GenericDevicePropertyMetadata metadata = argumentsMetadata[i];
                this.argumentsMetadata.put(metadata.getName(), metadata);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(GenericDeviceActionContext sac)
            throws GenericDeviceException {
    	GenericDeviceAccessSecurity.checkExecutePermission(getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
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
    	if(input == null) return output;
    	
    	String keys[] = output.getNames();
    	for (int i = 0; i < keys.length; i++){
    		if(! input.hasProperty(keys[i])) continue;
    		
    		Object value = input.getValue(keys[i]);
    		if (value instanceof String)
    			output.setStringValue(keys[i], (String)value);
    		else if (value instanceof Integer)
    			output.setIntValue(keys[i], ((Integer)value).intValue());
    		else if (value instanceof Float)
    			output.setFloatValue(keys[i], ((Float)value).floatValue());
    		else if (value instanceof Long)
    			output.setLongValue(keys[i], ((Long)value).longValue());
    	}
    	
    	metadataUtil.verifyPropertiesAgainstMetadata(output, argumentsMetadata);
        
		return output;
	}

	/**
     * {@inheritDoc}
     */
    public GenericDevicePropertyMetadata[] getResultMetadata() {
        return (GenericDevicePropertyMetadata[]) resultMetadata.values().toArray(
                new GenericDevicePropertyMetadata[resultMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public GenericDevicePropertyMetadata[] getArgumentsMetadata() {
        return (GenericDevicePropertyMetadata[]) argumentsMetadata.values().toArray(
                new GenericDevicePropertyMetadata[argumentsMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public GenericDeviceProperties createArguments() {
        return new GenericDevicePropertiesImpl(
                (GenericDevicePropertyMetadata[]) argumentsMetadata.values().toArray(
                        new GenericDevicePropertyMetadata[argumentsMetadata.size()]));
    }

    /**
     * {@inheritDoc}
     */
    public GenericDeviceProperties createResult() {
        return new GenericDevicePropertiesImpl(
                (GenericDevicePropertyMetadata[]) resultMetadata.values().toArray(
                        new GenericDevicePropertyMetadata[resultMetadata.size()]));
    }

    /**
     * {@inheritDoc}
     */
    public GenericDeviceActionContext createActionContext() {
        return new GenericDeviceActionContextImpl(createArguments(), createResult());
    }


    /**
     * {@inheritDoc}
     */
    public String getPath(boolean isAbsolute) {
        return path + "/action/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/action/" + this.getName();
    }


    /**
     * {@inheritDoc}
     */
    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
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
    public String getSerializedNode(String path, int format)
            throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (path == null)
            throw new GenericDeviceException(405, "Path cannot be null");

        if (path.length() == 0) {
            return serialize(format);
        } else if (path.equals("name")) {
            return getName();
        } else if (path.startsWith("arguments") && argumentsMetadata.size() > 0) {
            return metadataUtil.metadataToJson(path, format, "arguments", argumentsMetadata.values());
        } else if (path.startsWith("result") && resultMetadata.size() > 0) {
            return metadataUtil.metadataToJson(path, format, "result", resultMetadata.values());
        } else {
            throw new GenericDeviceException(404, "No such node found");
        }
    }

    private String toJsonString(int format, int indent)
            throws GenericDeviceException {
        String json = "{";
        json += "\"name\":\"" + Utils.escapeJSON(getName()) + "\"";
        StringBuffer sb = new StringBuffer(",");
        if (argumentsMetadata != null && argumentsMetadata.size() > 0) {
            sb.append(metadataUtil.metadataToJson("", Serializable.FORMAT_JSON, "arguments", argumentsMetadata.values())).append(',');
        }
        if (resultMetadata != null && resultMetadata.size() > 0) {
            sb.append(metadataUtil.metadataToJson("", Serializable.FORMAT_JSON, "result", resultMetadata.values())).append(',');
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
