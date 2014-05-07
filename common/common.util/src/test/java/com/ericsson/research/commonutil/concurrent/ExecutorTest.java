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

package com.ericsson.research.commonutil.concurrent;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class ExecutorTest {

    @Test
    public void test() throws InterruptedException {
        final int[] counter = new int[1];
        Executor executor = new Executor("test");
        Thread.sleep(100);

        executor.accept(new Runnable() {
            public void run() {
                counter[0]++;
            }
        });
        executor.accept(new Runnable() {
            public void run() {
                counter[0]++;
            }
        });
        executor.accept(new Runnable() {
            public void run() {
                counter[0]++;
            }
        });

        Thread.sleep(100);

        assertEquals(3, counter[0]);

        executor.terminate();

        try {
            executor.accept(new Runnable() {
                public void run() {
                    counter[0]++;
                }
            });
            fail();
        } catch (IllegalStateException e) {
            // success
        }
    }

    @Test
    public void testParallell() throws InterruptedException {
        final Map<Thread, Integer> threads = new HashMap<Thread, Integer>();
        Executor executor = new Executor("test", 10);
        Thread.sleep(100);

        for (int i = 0; i < 100; i++) {
            executor.accept(new Runnable() {
                public void run() {
                    Integer ctr = threads.get(Thread.currentThread());
                    if (ctr == null) {
                        threads.put(Thread.currentThread(), 1);
                    } else {
                        threads.put(Thread.currentThread(), ++ctr);
                    }
                }
            });
        }

        Thread.sleep(1000);

        assertTrue(threads.size() > 1);

        executor.terminate();

        try {
            executor.accept(new Runnable() {
                public void run() {
                }
            });
            fail();
        } catch (IllegalStateException e) {
            // success
        }

    }
}
