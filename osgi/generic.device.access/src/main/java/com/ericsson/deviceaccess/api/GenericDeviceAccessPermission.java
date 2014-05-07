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

package com.ericsson.deviceaccess.api;

import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public final class GenericDeviceAccessPermission extends BasicPermission {
    /**
     *
     */
    private static final long serialVersionUID = 1390917155441371447L;
    public static final String GET = "get";
    public static final String SET = "set";
    public static final String EXECUTE = "execute";

    private static final int ACTION_GET = 0x00000001;
    private static final int ACTION_SET = 0x00000002;
    private static final int ACTION_EXECUTE = 0x00000004;
    private static final int ACTION_ALL = ACTION_GET | ACTION_SET | ACTION_EXECUTE;
    private static final int ACTION_NONE = 0;

    private int actionMask;

    /**
     * Construct a named GenericDeviceAccessPermission for a set of actions to be permitted.
     * 
     * @param name
     * @param actions A comma separated string of actions: GET, SET and EXECUTE.
     */
    public GenericDeviceAccessPermission(String name, String actions) {
        super(name);
        actionMask = getActionMask(actions);
    }

    private GenericDeviceAccessPermission(String name, int mask) {
        super(name);
        actionMask = mask;
    }

    private int getActionMask(String actStr) {
        int mask = 0;
        if (actStr == null) return mask;
        StringTokenizer st = new StringTokenizer(actStr, ",");
        while (st.hasMoreElements()) {
            // TODO: Check if this works properly
            String action = st.nextToken().trim();
            System.out.println("getActionMask: " + action);
            if (GET.equalsIgnoreCase(action)) {
                mask |= ACTION_GET;
            } else if (SET.equalsIgnoreCase(action)) {
                mask |= ACTION_SET;
            } else if (EXECUTE.equalsIgnoreCase(action)) {
                mask |= ACTION_EXECUTE;
            }
        }
        return mask;
    }

    private int getMask() {
        return actionMask;
    }

    public boolean implies(Permission p) {
        if (p instanceof GenericDeviceAccessPermission) {
            GenericDeviceAccessPermission target = (GenericDeviceAccessPermission) p;

            return (((actionMask & target.actionMask) == target.actionMask) && super
                    .implies(p));
        }
        return (false);
    }

    public String getActions() {
        String actions = "";
        if ((actionMask & ACTION_GET) == ACTION_GET) {
            actions += GET;
        }
        if ((actionMask & ACTION_SET) == ACTION_SET) {
            if (actions.length() > 0) actions += ",";
            actions += SET;
        }
        if ((actionMask & ACTION_EXECUTE) == ACTION_EXECUTE) {
            if (actions.length() > 0) actions += ",";
            actions += EXECUTE;
        }
        return actions;
    }

    public PermissionCollection newPermissionCollection() {
        return new GenericDeviceAccessPermissionCollection();
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return Hash code value for this object.
     */
    public int hashCode() {
        return (getName().hashCode() ^ getActions().hashCode());
    }

    class GenericDeviceAccessPermissionCollection extends PermissionCollection {

        /**
         *
         */
        private static final long serialVersionUID = 1102307291093157855L;
        private Hashtable permissions;
        private boolean allAllowed = false;

        public GenericDeviceAccessPermissionCollection() {
            permissions = new Hashtable();
        }

        public void add(Permission perm) {
            if (!(perm instanceof GenericDeviceAccessPermission)) {
                throw new IllegalArgumentException("invalid permission: "
                        + perm);
            }
            if (isReadOnly()) {
                throw new SecurityException("readonly PermissionCollection");
            }
            String name = perm.getName();
            GenericDeviceAccessPermission gdaPerm = (GenericDeviceAccessPermission) perm;
            GenericDeviceAccessPermission existing = (GenericDeviceAccessPermission) permissions.get(name);

            if (existing != null) {
                int oldMask = existing.getMask();
                int newMask = gdaPerm.getMask();
                if (oldMask != newMask) {
                    permissions.put(name,
                            new GenericDeviceAccessPermission(name, oldMask | newMask));

                }
            } else {
                permissions.put(name, perm);
            }

            if (!allAllowed) {
                if (name.equals("*"))
                    allAllowed = true;
            }
        }

        public Enumeration elements() {
            return permissions.elements();
        }

        public boolean implies(Permission perm) {
            if (!(perm instanceof GenericDeviceAccessPermission)) {
                return false;
            }
            GenericDeviceAccessPermission gdaPerm = (GenericDeviceAccessPermission) perm;
            GenericDeviceAccessPermission x;

            int desired = gdaPerm.getMask();
            int effective = 0;

            // Short cut if we have "*"
            if (allAllowed) {
                x = (GenericDeviceAccessPermission) permissions.get("*");
                if (x != null) {
                    effective |= x.getMask();
                    if ((effective & desired) == desired)
                        return (true);
                }
            }


            x = (GenericDeviceAccessPermission) permissions.get(gdaPerm.getName());

            if (x != null) {
                // we have a direct hit!
                effective |= x.getMask();
                if ((effective & desired) == desired)
                    return (true);
            }
            /*
                * We only care direct match for now since all concerned classes
                * are under com.ericsson.deviceaccess.api. We may need to
                * consider to implement handling of package names and wild cards.
                * See BundlePermision implementation in Knopflerfish, for example.
                * -- Kenta
                */

            return false;
        }

    }
}
