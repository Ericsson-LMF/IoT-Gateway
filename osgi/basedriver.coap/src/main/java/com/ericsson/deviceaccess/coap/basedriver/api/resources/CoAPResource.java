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
package com.ericsson.deviceaccess.coap.basedriver.api.resources;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a CoAPResource on a CoAP endpoint, which is cached on
 * this CoAP gateway server.
 */
public class CoAPResource {

    public static final String DELIMITER = ",";

    public static final String ATTRIBUTE_DELIMITER = ";";

    public static final String RESOURCE_TYPE = "rt";

    public static final String INTERFACE_DESCRIPTION = "if";

    public static final String MAXIMUM_SIZE = "sz";

    public static final String TITLE = "title";

    public static final String TITLE_ASTERISK = "title*";

    public static final String RELATION_TYPE = "rel";

    public static final String ANCHOR = "anchor";

    public static final String CONTENT_TYPE = "ct";

    public static final String INSTANCE = "ins"; // from the
    // draft-shelby-core-resource-directory-02

    // these variables are defined in the CoRE Link format 09 draft
    public static final String KEY = "key"; // for security demo

    /**
     * Resource type 'rt' attribute as defined in CoRE Link Format 09
     */
    private String resourceType;

    /**
     * Interface description 'if' attribute
     */
    private String interfaceDescription;

    /**
     * Maximum size estimate 'sz' attribute
     */
    private int maximumSize;

    // content-type ct definition is moved to coap core
    /**
     * Content-type code 'ct' attribute
     */
    private int contentType;

    /**
     * Maximum age for this information in seconds
     */
    private int maxAge;

    // web linking RFC 5988
    /**
     * Human-readable title
     */
    private String title;

    private String titleAsterisk;

    private String relationType;

    private String anchor;

    /**
     * Uri to the resource
     */
    private URI resourceIdentifier;

    private final List<CoAPResourceObserver> observers;

    private byte[] content;

    private CoAPResourceType type;

    private String host;

    private String instance;

    private String key;

    /**
     * This map keeps the different link-format attribute values so that they
     * can be easily written into string
     */
    private final HashMap<String, String> values;


    /**
     * Constructor. A CoAP resource is identified using the URI
     * (coap://host:port/path)
     *
     * @param URI unique URI to the resource
     */
    public CoAPResource(URI uri) {
        this.resourceIdentifier = uri;
        this.contentType = -1;
        this.maximumSize = -1;
        this.interfaceDescription = "";
        this.resourceType = "";
        this.maxAge = 2; // default value by core 08
        this.observers = new LinkedList<>();
        this.title = "";
        this.titleAsterisk = "";
        this.relationType = "";
        this.type = CoAPResourceType.OTHER;
        this.values = new HashMap<>();
        this.host = "";
    }

    /**
     * Set the URI for this resource
     *
     * @param uri unique URI for this resource
     */
    public void setUri(URI uri) {
        this.resourceIdentifier = uri;
    }

    /**
     * Get the URI of the resource
     *
     * @return URI identifying the resource
     */
    public URI getUri() {
        return this.resourceIdentifier;
    }

    public void setHost(String host) {
        this.host = host;

    }

    public String getHost() {
        return this.host;
    }

    /**
     * This method creates a link format presentation of the CoAP resource.
     *
     * @param absolute whether or not the URI path returned in the should be
     * absolute (or relative if value false)
     * @return link format presentation of the resource as String
     * @throws CoAPException
     */
    public String getLinkFormatPresentation(boolean absolute)
            throws CoAPException {
        StringBuilder linkformat = new StringBuilder();

        if (absolute && this.resourceIdentifier.getHost() == null && host.isEmpty()) {
            throw new CoAPException("No host defined for the resource");
        }

        if (!absolute) {
            linkformat.append("<").append(resourceIdentifier.getPath()).append(">");
        } else {
            String path = resourceIdentifier.toString();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            String h = "";
            if (resourceIdentifier.getHost() != null) {
                h = resourceIdentifier.getHost();
            } else if (!host.isEmpty()) {
                h = host;
            }

            linkformat.append("<coap://").append(h).append(path).append(">");

        }

        values.forEach((k, v) -> {
            linkformat.append(";");
            linkformat.append(k).append("=").append(v);
        });
        return linkformat.toString().trim();
    }

    /**
     * Set the enumerated resource type of the resource. These enumerated types
     * are based on the resources
     *
     * @param type type of the resource
     */
    public void setCoAPResourceType(CoAPResourceType type) {
        this.type = type;
    }

    /**
     * Get the enumerated resource type of the resource
     *
     * @return type of the resource
     */
    public CoAPResourceType getCoAPResourceType() {
        return this.type;
    }

    /**
     * Set the "rt" attribute as defined in the link format
     *
     * @param resourceType
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
        this.values.put(RESOURCE_TYPE, this.resourceType);
    }

    /**
     * Get the "rt" attribute (defined in CoRE Link Format 07)
     *
     * @return value for the "rt" attribute
     */
    public String getResourceType() {
        return this.resourceType;
    }

    /**
     * Get the "if" attribute (defined in CoRE Link Format 07)
     *
     * @return value for the "if" attribute
     */
    public String getInterfaceDescription() {
        return this.interfaceDescription;

    }

    /**
     * Set the "if" attribute
     *
     * @param interfaceDescription value to set
     */
    public void setInterfaceDescription(String interfaceDescription) {
        this.interfaceDescription = interfaceDescription;
        this.values.put(INTERFACE_DESCRIPTION, this.interfaceDescription);
    }

    /**
     * Set the "ct" attribute (defined in coap core 07 draft)
     *
     * @param contentType value to set
     */
    public void setContentType(int contentType) {
        this.contentType = contentType;
        this.values.put(CONTENT_TYPE, Integer.toString(contentType));
    }

    /**
     * Get the "ct" attribute
     *
     * @return value of the "ct" attribute
     */
    public int getContentType() {
        return this.contentType;
    }

    /**
     * Set the content of the resource
     *
     * @param content
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Get the content of the resource
     *
     * @return content of the resource
     */
    public byte[] getContent() {
        return this.content;
    }

    /**
     * Set the "sz" attribute (defined in draft-ietf-core-link-format-07)
     *
     * @param maximumSize
     */
    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        this.values.put(MAXIMUM_SIZE, Integer.toString(maximumSize));
    }

    /**
     * Get the "sz" attribute
     *
     * @return "sz" attribute
     */
    public int getMaximumSize() {
        return maximumSize;
    }

    /**
     * Set maximum time in seconds this resource should be stored
     *
     * @param maxAge maximum age in seconds
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Get maximum time for this resource to be cached in seconds
     *
     * @return maximum time to cache this resource in seconds
     */
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * Set the title (defined in RFC 5988)
     *
     * @param title human-readable title
     */
    public void setTitle(String title) {
        this.title = title;
        this.values.put(TITLE, title);
    }

    /**
     * Get the title
     *
     * @return human-readable title for this resource
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set title* (defined in RFC 5988)
     *
     * @param title
     */
    public void setTitleAsterisk(String title) {
        this.titleAsterisk = title;
        this.values.put(TITLE_ASTERISK, title);
    }

    /**
     * Get title*
     *
     * @return title*
     */
    public String getTitleAsterisk() {
        return this.titleAsterisk;
    }

    /**
     * Set the relation type (defined in RFC 5988)
     *
     * @param relationType
     */
    public void setRelationType(String relationType) {
        this.relationType = relationType;
        this.values.put(RELATION_TYPE, relationType);
    }

    /**
     * Get the relation type (defined in RFC 5988)
     *
     * @return
     */
    public String getRelationType() {
        return this.relationType;
    }

    /**
     * Set the anchor (RFC 5988)
     *
     * @param anchor
     */
    public void setAnchor(String anchor) {
        this.anchor = anchor;
        this.values.put(ANCHOR, anchor);
    }

    /**
     * Get the anchor
     *
     * @return value of the anchor
     */
    public String getAnchor() {
        return this.anchor;
    }

    public void setInstance(String instance) {
        this.instance = instance;
        this.values.put(INSTANCE, instance);
    }

    /**
     * Add an observer on this resource. A resource can have several observers.
     *
     * @param observer observer to be added
     */
    public void addObserver(CoAPResourceObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Remove an observer from this resource.
     *
     * @param observer to be removed
     * @return true if the observer was successfully removed from this resource
     */
    public boolean removeObserver(CoAPResourceObserver observer) {
        boolean removed = this.observers.remove(observer);
        return removed;
    }

    /**
     * Get the list of observers that are subscribing to the changes of this
     * resource
     *
     * @return list of observers
     */
    public List<CoAPResourceObserver> getObservers() {
        return this.observers;
    }

    public HashMap<String, String> getAttributes() {
        return this.values;
    }

    /**
     * Return the value based on the given attributename, or null if no value is
     * found with the given key
     *
     * @param attributeName name of the attribute to be found
     * @return attribute value or null if not found
     */
    public String getAttribute(String attributeName) {
        return values.get(attributeName);
    }

    /**
     * This method sets the key attribute. Note that the key attribute is used
     * in a security demo, it's not an official link format attribute
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
        values.put(KEY, this.key);
    }

    public String getKey() {
        return this.key;
    }

    /**
     * This method is to check whether or not a resource matches given query
     * criteria. It compares the parameters and parameter values found in a
     * request with the current resource.
     *
     * @param params request parameters to match to
     * @return boolean value indicating whether or not this resource matches the
     * given parameters
     */
    public boolean matchWithQueryParams(HashMap<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> values.containsKey(e.getKey()))
                .map(e -> {
                    String v = e.getValue();
                    if (!v.startsWith("\"") && !v.endsWith("\"")) {
                        v = "\"" + v + "\"";
                    }
                    e.setValue(v);
                    return e;
                })
                .filter(e -> values.get(e.getKey()).equals(e.getValue()))
                .count() == params.size();
    }

    // TODO add subresources/information about the parent (maybe not needed in
    // the first phase)
    /*
     * The following enum is to represent the resources that are received from
     * californium server
     */
    public static enum CoAPResourceType {

        CARELESS("/careless"), FEEDBACK("/feedback"), HELLO_WORLD("/helloWorld"), LARGE("/large"), MIRROR("/mirror"), SEPARATE("/separate"), STORAGE("/storage"), TIME_RESOURCE("/timeResource"), TO_UPPER("/toUpper"), WEATHER_RESOURCE("/weatherResource"), OTHER("/other")
        private final String path;

        private CoAPResourceType(String path) {
            this.path = path;
        }

        public String getPath() {
            return this.path;
        }
    }

}
