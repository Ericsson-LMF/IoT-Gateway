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

import com.ericsson.research.commonutil.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * An active component accepting jobs via a queue and runs them in a separate thread.
 */
public class Executor {
    private static final Logger logger = LoggerFactory.getLogger(Executor.class);
    private static int id;
    private final List jobQueue = new LinkedList();
    private volatile boolean executorRunning;
    private int numberOfThreads;
    private Worker[] workers;
    private String name;

    public Executor() {
        this(null);
    }

    /**
     * Creates it and starts it.
     */
    public Executor(String name) {
        this(name, 1);
    }

    public Executor(String name, int numberOfThreads) {
        this.name = name != null ? name : "Executor-"+(id++);
        this.numberOfThreads = numberOfThreads;
        executorRunning = true;
        workers = new Worker[numberOfThreads];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-"+name+"-"+i);
            workers[i].start();
        }
    }

    /**
     * Accepts a job and puts it into the queue.
     *
     * @param job
     */
    public void accept(Runnable job) {
        if (!executorRunning) {
            throw new IllegalStateException("Tried to send job to stopped executor");
        }
        synchronized (jobQueue) {
            jobQueue.add(job);
            jobQueue.notifyAll();
            logger.debug("Added job; queue=" + jobQueue);
        }
    }

    /**
     * Terminates this executor. Interrupts the worker thread and discards any waiting jobs.
     */
    public void terminate() {
        logger.debug("Stopping " + this);
        executorRunning = false;
        for (int i = 0; i < workers.length; i++) {
            Worker worker = workers[i];
            worker.interrupt();
        }
        synchronized (jobQueue) {
            jobQueue.notifyAll();
        }
    }

    public String toString() {
        return "Executor{" +
                "name='"+name + '\'' +
                ", workers=" + StringUtil.toString(workers) +
                ", running=" + executorRunning +
                '}';
    }

    private class Worker extends Thread{
        private String name;
        private boolean workerRunning;

        private Worker(String name) {
            super(name);
            this.name = name;
        }

        public void run() {
            workerRunning = true;
            logger.debug(this + " has been started");
            outer:
            while (executorRunning) {
                Runnable job;
                synchronized (jobQueue) {
                    while (jobQueue.size() == 0) {
                        try {
                            // logger.debug(this + " waits for new jobs");
                            jobQueue.wait();
                        } catch (InterruptedException e) {
                            // Check if still active
                            continue outer;
                        }
                    }
                    job = (Runnable) jobQueue.remove(0);
                }

                try {
                    logger.debug(this + " executes job " + job);
                    job.run();
                } catch (Throwable e) {
                    logger.warn("Exception when executing job in executor.", e);
                }
            }
            workerRunning = false;
            logger.debug(this + " has been stopped");
        }

        public String toString() {
            return "Worker{" +
                    "name='" + name + "\'" +
                    ", running=" + workerRunning +
                    '}';
        }
    }
}
