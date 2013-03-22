package com.ericsson.deviceaccess.api;

/**
 * A set of named properties.
 */
public interface GenericDeviceProperties extends Serializable {
    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDeviceProperties {
    }

	static final String CURRENT_VALUE = "currentValue";
	static final String METADATA = "metadata";

	/**
	 * Tester to check if a property identified by the key is in the object or not.
	 * 
	 * @param key Name of the property in question.
	 * @return Whether or not the object contains a property with the key
	 */
	public boolean hasProperty(String key);
	
    /**
     * Getter for a property identified by the key that returns the value as
     * integer.
     *
     * @param key Name of the property in question.
     * @return Value of the property identified by the parameter "key". Returns
     *         default value from service schema if the value is not set.
     */
    public int getIntValue(String key);

    /**
     * Getter for a property identified by the key that returns the value as
     * long.
     *
     * @param key Name of the property in question.
     * @return Value of the property identified by the parameter "key". Returns
     *         default value from service schema if the value is not set.
     */
    public long getLongValue(String key);

    /**
     * Getter for a property identified by the key that returns the value as
     * float.
     *
     * @param key Name of the property in question.
     * @return Value of the property identified by the parameter "key". Returns
     *         default value from service schema if the value is not set.
     */
    public float getFloatValue(String key);

    /**
     * Setter for an long property identified by the key.
     *
     * @param key   Name of the property in question.
     * @param value An integer value to be set for the property.
     */
    public void setLongValue(String key, long value);

    /**
     * Setter for an integer property identified by the key.
     *
     * @param key   Name of the property in question.
     * @param value An integer value to be set for the property.
     */
    public void setIntValue(String key, int value);

    /**
     * Setter for a float property identified by the key.
     *
     * @param key   Name of the property in question.
     * @param value A float value to be set for the property.
     */
    public void setFloatValue(String key, float value);

    /**
     * Getter for the type of the property identified by the key.
     *
     * @param key Name of the property in question.
     * @return Simple class name of the value identified by the key. Null if no
     *         property is found by the key.
     */
    public String getValueType(String key);

    /**
     * Getter for a property identified by the key that returns the value as
     * string.
     *
     * @param key Name of the property in question.
     * @return Value of the property identified by the parameter "key". Returns
     *         default value from service schema if the value is not set.
     */
    public String getStringValue(String key);

    /**
     * Setter for a string property identified by the key.
     * If the metadata indicates that this is a number the string is parsed to
     * that number type.
     *
     * @param key   Name of the property in question.
     * @param value A string value to be set for the property.
     */
    public void setStringValue(String key, String value);

    /**
     * Getter for an array of the keys found in the property list.
     *
     * @return An array of the keys.
     */
    public String[] getNames();

    /**
     * Gets the value with the specified name. The only allowed types are
     * {@link String}, {@link Integer} and {@link Float}
     *
     * @param name
     * @return
     */
    public Object getValue(String name);

    /**
     * Adds all elements from the specified properties.
     *
     * @param source
     */
    public void addAll(GenericDeviceProperties source);

    /**
     * Serializes the state (i.e. values of all properties) to JSON
     * @return
     *         Example:
     *         <code>{"property1" : "99","property2" : "99"}</code>
     *
     */
    String serializeState();
}
