/*
 * User: joel
 * Date: 2010-nov-10
 * Time: 11:07:10
 *
 * Copyright (c) Ericsson AB, 2010.
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
package com.ericsson.research.connectedhome.common;

import com.ericsson.research.connectedhome.common.server.util.warp.AuthIdentity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers.
 */
public class Matchers {

    public static Matcher<AuthIdentity> anAuthIdentity(final AuthIdentity expected) {
        return new TypeSafeMatcher<AuthIdentity>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean matchesSafely(AuthIdentity actual) {
                return actual.getUser().equals(expected.getUser()) &&
                    actual.getPassword().equals(expected.getPassword());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void describeTo(Description descr) {
                descr.appendText("Warp user credentials equals ").appendValue(expected);
            }
        };
    }
}
