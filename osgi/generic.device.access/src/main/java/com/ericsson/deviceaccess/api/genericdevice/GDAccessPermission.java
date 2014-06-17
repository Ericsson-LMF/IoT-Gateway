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

import com.ericsson.research.commonutil.LegacyUtil;
import com.ericsson.research.commonutil.function.FunctionalUtil;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GDAccessPermission extends BasicPermission {

    /**
     *
     */
    private static final long serialVersionUID = 1390917155441371447L;

    private final EnumSet<Type> actionMask;

    /**
     * Construct a named GenericDeviceAccessPermission for a set of actions to
     * be permitted.
     *
     * @param name
     * @param actions A comma separated string of actions: GET, SET and EXECUTE.
     */
    public GDAccessPermission(String name, String actions) {
        super(name);
        actionMask = getActionMask(actions);
    }

    public GDAccessPermission(String name, EnumSet<Type> actionMask) {
        super(name);
        this.actionMask = actionMask.clone();
    }

    public GDAccessPermission(String name, Type first, Type... rest) {
        super(name);
        this.actionMask = EnumSet.of(first, rest);
    }

    private EnumSet<Type> getActionMask(String actStr) {
        EnumSet<Type> result = EnumSet.noneOf(Type.class);
        if (actStr == null) {
            return result;
        }
        for (String action : actStr.split(",")) {
            action = action.trim();
            System.out.println("getActionMask: " + action);
            result.add(Type.get(action));
        }
        return result;
    }

    protected EnumSet<Type> getMask() {
        return actionMask;
    }

    @Override
    public boolean implies(Permission permission) {
        AtomicBoolean flag = new AtomicBoolean(false);
        FunctionalUtil.doIfCan(GDAccessPermission.class, permission, target -> {
            flag.set(actionMask.containsAll(target.actionMask) && super.implies(target));
        });
        return flag.get();
    }

    @Override
    public String getActions() {
        StringBuilder builder = new StringBuilder();
        actionMask.forEach(type -> builder.append(type.get()).append(","));
        if (!actionMask.isEmpty()) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    public PermissionCollection newPermissionCollection() {
        return new GenericDeviceAccessPermissionCollection();
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return Hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() ^ getActions().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        FunctionalUtil.doIfCan(GDAccessPermission.class, obj, target -> {
            if (getName().equals(target.getName())) {
                flag.set(getMask().equals(target.getMask()));
            }
        });
        return flag.get();
    }

    class GenericDeviceAccessPermissionCollection extends PermissionCollection {

        /**
         *
         */
        private static final long serialVersionUID = 1102307291093157855L;
        private final Map<String, GDAccessPermission> permissions;
        private boolean allAllowed = false;

        GenericDeviceAccessPermissionCollection() {
            permissions = new HashMap<>();
        }

        @Override
        public void add(Permission perm) {
            if (isReadOnly()) {
                throw new SecurityException("readonly PermissionCollection");
            }
            if (!FunctionalUtil.doIfCan(GDAccessPermission.class, perm, gdaPerm -> {
                String name = gdaPerm.getName();
                permissions.compute(name, (key, value) -> {
                    if (value == null) {
                        return gdaPerm;
                    }
                    value.getMask().addAll(gdaPerm.getMask());
                    return value;
                });
                if (!allAllowed) {
                    if (name.equals("*")) {
                        allAllowed = true;
                    }
                }
            })) {
                throw new IllegalArgumentException("invalid permission: " + perm);
            }
        }

        @Override
        public Enumeration elements() {
            return LegacyUtil.toEnumeration(permissions.values().iterator());
        }

        @Override
        public boolean implies(Permission perm) {
            AtomicBoolean flag = new AtomicBoolean(false);
            FunctionalUtil.doIfCan(GDAccessPermission.class, perm, gdaPerm -> {
                EnumSet<Type> desired = gdaPerm.getMask();
                EnumSet<Type> effective = EnumSet.noneOf(Type.class);

                // Shortcut if we have "*"
                if (allAllowed) {
                    GDAccessPermission temp = permissions.get("*");
                    if (temp != null) {
                        effective.addAll(temp.getMask());
                        if (effective.containsAll(desired)) {
                            flag.set(true);
                            return;
                        }
                    }
                }

                permissions.computeIfPresent(gdaPerm.getName(), (key, value) -> {
                    // we have a direct hit!
                    effective.addAll(value.getMask());
                    if (effective.containsAll(desired)) {
                        flag.set(true);
                    }
                    return value;
                });
                /*
                 * We only care direct match for now since all concerned classes
                 * are under com.ericsson.deviceaccess.api. We may need to
                 * consider to implement handling of package names and wild cards.
                 * See BundlePermision implementation in Knopflerfish, for example.
                 * -- Kenta
                 */
            });
            return flag.get();
        }

    }

    public enum Type {

        GET("get", 0x00000001),
        SET("set", 0x00000002),
        EXECUTE("execute", 0x00000004),
        UNKNOWN("", 0x00000000);

        private final String string;
        private final int mask;

        Type(String string, int mask) {
            this.string = string;
            this.mask = mask;
        }

        public String get() {
            return string;
        }

        public int mask() {
            return mask;
        }

        public static Type get(String string) {
            string = string.toLowerCase();
            for (Type type : Type.values()) {
                if (type.get().equals(string)) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        public boolean isContained(int mask) {
            if (this == UNKNOWN) {
                return false;
            }
            return (mask & mask()) == mask();
        }
    }

}
