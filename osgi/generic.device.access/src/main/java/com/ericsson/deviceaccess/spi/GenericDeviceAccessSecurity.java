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

package com.ericsson.deviceaccess.spi;

import com.ericsson.deviceaccess.api.GenericDeviceAccessPermission;

/**
 * In GenericDeviceImpl class, as well as other SPI classes such as
 * GenericDeviceServiceImpl, each method call checks if the caller is authorized
 * to call the method by using this class. A call may have GET, SET, and EXECUTE
 * permissions corresponding to retrieving information from a device, setting
 * information to a device, and executing an action of a service on a device respectively.
 */
public class GenericDeviceAccessSecurity {

	public static void checkGetPermission(String clazz) {
		checkPermission(clazz, GenericDeviceAccessPermission.GET);
	}

	public static void checkSetPermission(String clazz) {
		checkPermission(clazz, GenericDeviceAccessPermission.SET);
	}

	public static void checkExecutePermission(String clazz) {
		checkPermission(clazz, GenericDeviceAccessPermission.EXECUTE);
	}

	private static void checkPermission(String clazz, String action) {
		SecurityManager sm = System.getSecurityManager();
		if (null != sm) {
			sm.checkPermission(new GenericDeviceAccessPermission(clazz, action));
		}
	}

}
