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
