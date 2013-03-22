/*
 * Copyright (c) Ericsson AB, 2013.
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
package com.ericsson.deviceaccess.api;

/**
 * GenericDeviceContextNode represents a node in a tree built by Generic devices and their services and actions.
 * This class provides operations to identify a generic device, a service, or an action in the tree.
 * 
 */
public interface GenericDeviceContextNode extends Serializable {

    /**
     * @param isAbsolute true if it is supposed to return absolute path from the root.
     *                   false if relative path from the device node is wanted.
     * @return path to the node with "/" as delimiter.
     * @deprecate Method to get path to this node. This method is used in Web Device Connectivity,
     * but is deprecated. getPath() without an argument should be used instead.
     */
    public String getPath(boolean isAbsolute);

    /**
     * @param pathToParent
     * @deprecate Method to update path until the node's parent by the specified value. This method is used in
     * Web Device Connectivity, but is deprecated.
     */
    public void updatePath(String pathToParent);

    /**
     * Method to get path from device node to this node.
     *
     * @return path from the device node to the node with "/" as delimiter.
     */
    public String getPath();


    String getSerializedNode(String path, int format) throws GenericDeviceException;
}
