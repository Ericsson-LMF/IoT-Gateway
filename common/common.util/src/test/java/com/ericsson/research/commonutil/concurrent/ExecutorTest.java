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
