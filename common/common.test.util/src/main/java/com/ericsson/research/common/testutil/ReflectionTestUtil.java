/*
 * User: joel
 * Date: 2011-09-08
 * Time: 15:40
 *
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
