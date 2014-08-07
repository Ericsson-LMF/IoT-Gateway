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
package com.ericsson.deviceaccess.api.genericdevice;

import com.ericsson.common.util.serialization.Format;
import com.ericsson.deviceaccess.api.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * GenericDeviceContextNode represents a node in a tree built by Generic devices
 * and their services and actions. This class provides operations to identify a
 * generic device, a service, or an action in the tree.
 *
 */
public interface GDContextNode extends Serializable {

    /**
     * @param isAbsolute true if it is supposed to return absolute path from the
     * root. false if relative path from the device node is wanted.
     * @return path to the node with "/" as delimiter.
     * @deprecate Method to get path to this node. This method is used in Web
     * Device Connectivity, but is deprecated. getPath() without an argument
     * should be used instead.
     */
    String getPath(boolean isAbsolute);

    /**
     * @param pathToParent
     * @deprecate Method to update path until the node's parent by the specified
     * value. This method is used in Web Device Connectivity, but is deprecated.
     */
    void updatePath(String pathToParent);

    /**
     * Method to get path from device node to this node.
     *
     * @return path from the device node to the node with "/" as delimiter.
     */
    @JsonIgnore
    String getPath();

    String getSerializedNode(String path, Format format) throws GDException;
}
