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

package com.ericsson.research.common.testutil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utilities for reflection.
 */
public class ReflectionTestUtil {
    private ReflectionTestUtil() {
    }

    /**
     * Set the field value of the field with the given name of the specified object.
     * If the object is a class, then it is assumed to be a static field.
     *
     * @param object    the object
     * @param fieldName the field name
     * @param value     the value to set
     */
    public static void setField(Object object, String fieldName, Object value) {
        try {
            if (object instanceof Class) {
                Field field = findField((Class) object, fieldName, null);
                field.setAccessible(true);
                field.set(null, value);
            } else {
                Field field = findField(object.getClass(), fieldName, null);
                field.setAccessible(true);
                field.set(object, value);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Gets the field value of the field with the given name of the specified object.
     * If the object is a class, then it is assumed to be a static field.
     *
     * @param object    the object
     * @param fieldName the name of the field
     *
     * @return the field value of the field with the given name of the specified object
     */
    public static <T> T getField(Object object, String fieldName) {
        try {
            if (object instanceof Class) {
                Field field = findField((Class) object, fieldName, null);
                field.setAccessible(true);
                return (T) field.get(null);
            } else {
                Field field = findField(object.getClass(), fieldName, null);
                field.setAccessible(true);
                return (T) field.get(object);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Invokes the method with the given name of the specified object, with the specified parameters.
     * If the object is a class, then it is assumed to be a static method.
     *
     * @param object     the object
     * @param methodName the
     * @param parameters the parameters
     *
     * @return the method return value of the dispatched method
     */
    public static <T> T invokeMethod(Object object, String methodName, Object... parameters) {
        try {
            Class[] parameterTypes = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Object arg = parameters[i];
                parameterTypes[i] = arg.getClass();
            }

            if (object instanceof Class) {
                Method method = findMethod((Class) object, methodName, parameterTypes);
                method.setAccessible(true);
                return (T) method.invoke(null, parameters);
            } else {
                Method method = findMethod(object.getClass(), methodName, parameterTypes);
                method.setAccessible(true);
                return (T) method.invoke(object, parameters);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and parameter types. Searches all superclasses up to <code>Object</code>.
     * <p>Returns <code>null</code> if no {@link Method} can be found.
     *
     * @param clazz      the class to introspect
     * @param name       the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be <code>null</code> to indicate any signature)
     *
     * @return the Method object, or <code>null</code> if none found
     */
    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                if (name.equals(method.getName())
                    && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied <code>name</code> and/or {@link Class type}. Searches all
     * superclasses up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field (may be <code>null</code> if type is specified)
     * @param type  the type of the field (may be <code>null</code> if name is specified)
     *
     * @return the corresponding Field object, or <code>null</code> if not found
     */
    private static Field findField(Class<?> clazz, String name, Class<?> type) {
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }
}
