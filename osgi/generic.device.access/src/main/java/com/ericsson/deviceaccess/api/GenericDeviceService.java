package com.ericsson.deviceaccess.api;

/**
 * A GenericDeviceService represents a service provided by a generic device.
 * A GenericDeviceService can have zero or more properties and actions.
 * 
 */
public interface GenericDeviceService extends GenericDeviceContextNode {
    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDeviceService {
    }

    /**
     * Method to query an action that is available in the service by its name.
     *
     * @param name Name of the action in question.
     * @return An instance of an action that has the name. Null if not found.
     */
    public GenericDeviceAction getAction(String name);

    /**
     * Getter for the name of the service.
     *
     * @return Name of the service.
     */
    public String getName();

    /**
     * Getter for the GenericDeviceProperties object that represents the list of
     * properties of the service.
     *
     * @return List of properties of the service.
     */
    public GenericDeviceProperties getProperties();

    /**
     * Gets the metadata for the properties of this service.
     *
     * @return the metadata for the properties of this service.
     */
    public GenericDevicePropertyMetadata[] getPropertiesMetadata();

    /**
     * Method to return an array of action names to iterate actions supported in
     * the service.
     *
     * @return array of action names
     */
    public String[] getActionNames();

    /**
     * Serializes the state (i.e. values of all properties in the service) to JSON
     * @return JSON of the state.
     *         Example:
     *         <code>{"property1" : "99","property2" : "99"}</code>
     *
     */
    public String serializeState();
}
