/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.ericsson.research.connectedhome.common.server.util.warp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Warp user credentials
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthIdentity {
    private String user;
    private String password;
    private String origin = "dlnaserver";

    /**
     * For deserialization
     */
    protected AuthIdentity() {
        // for deserialization
    }
    
    /**
     * Constructor.
     * @param userName user name
     * @param password password
     */
    public AuthIdentity(String userName, String password) {
        this.user = userName;
        this.password = password;
    }

    /**
     * Gets user name.
     * @return user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets user name.
     * @param userName user name
     */
    public void setUser(String userName) {
        this.user = userName;
    }

    /**
     * Gets password.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets password.
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the origin.
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Gets the origin.
     * @param origin the origin
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AuthIdentity{" +
            "user='" + user + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}
