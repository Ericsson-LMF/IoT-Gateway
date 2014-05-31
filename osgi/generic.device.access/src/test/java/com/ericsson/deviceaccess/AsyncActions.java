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
package com.ericsson.deviceaccess;

import java.util.*;

/**
 * Test to implement support for. Sending a request and filling in the result in
 * a provided object. write something like
 * <p/>
 * my actor { on request from client: send request request to request.device on
 * response from device send response to client
 * <p/>
 * <p/>
 * action context innehåller 1) device:service:action+arguments 2) sender +
 * empty result
 * <p/>
 * 1. Hitta device.service.action och anropa executeAsync(ac): släpper direkt 2.
 * Adaptor utför action och fyller i resultat 2. Adaptor anropar AC.reply(),
 * vilken kommer att anropa mekanism given från client att sända resultatet
 * <p/>
 * <p/>
 * }
 */
public class AsyncActions {

    interface Device {

        Service getService(String serviceName);
    }

    interface Service {

        Action getAction(String actionName);
    }

    interface Result {

    }

    interface Actions {

        void setResultRecipient(ResultRecipient recipient);

        void aggregateResults(boolean aggregate);

        List getActionContexts();

        interface ResultRecipient {

            void onCompleted(List<? extends ActionContext> completedActions);
        }

        class SerializationSupport {

            public static Actions deserialize(String data) {
                return new TestActions();
            }
        }
    }

    interface ActionContext {

        void sendResult(); // does recipient.accept(this);

        Result getResult();

        String getActionName();

        String getDeviceId();

        String getServiceName();

        Throwable getFailure();

        void setResultRecipient(ResultRecipient actionResultRecipient);

        void setFailure(Throwable e);

        interface ResultRecipient {

            void onCompleted(ActionContext ctx);
        }

    }

    interface Action {

        void executeAsyncWith(ActionContext ctx);
        // put ctx into jobQueue
        // notify exec thread
        // exec thread will take ctx and invoke the action, fill in the result and then invoke ctx.reply()

    }

    static class TestActions implements Actions, ActionContext.ResultRecipient {

        private ResultRecipient recipient;
        private List<? extends ActionContext> contexts = Arrays.asList(
                new TestActionContext("d1", "s1", "banan"),
                new TestActionContext("d2", "s2", "apa"),
                new TestActionContext("d2", "s1", "orange"),
                new TestActionContext("d3", "s1", "mango"));
        private Set completed = new HashSet(contexts.size());
        private boolean aggregate;

        public void setResultRecipient(ResultRecipient recipient) {
            this.recipient = recipient;
            for (Iterator iterator = contexts.iterator(); iterator.hasNext();) {
                ActionContext actionContext = (ActionContext) iterator.next();
                actionContext.setResultRecipient(this);
            }
        }

        public void aggregateResults(boolean aggregate) {
            this.aggregate = aggregate;
        }

        public List getActionContexts() {
            return contexts;
        }

        public void onCompleted(ActionContext ctx) {
            System.out.println(ctx + " was completed!");
            completed.add(ctx);
            if (aggregate) {
                if (completed.size() == contexts.size()) {
                    recipient.onCompleted(contexts);
                } else {
                    ArrayList c = new ArrayList(contexts);
                    c.removeAll(completed);
                    System.out.println("Waiting for " + c);
                }
            } else {
                recipient.onCompleted(Arrays.asList(ctx));
            }
        }

        @Override
        public String toString() {
            return contexts.toString();
        }

        private static class TestActionContext implements ActionContext {

            private String actionName;
            private ResultRecipient resultRecipient;
            private String serviceName;
            private String devId;
            private Throwable fail;

            public TestActionContext(String devId, String serviceName, String actionName) {
                this.devId = devId;
                this.serviceName = serviceName;
                this.actionName = actionName;
            }

            public void sendResult() {
                resultRecipient.onCompleted(this);
            }

            public Result getResult() {
                return new Result() {
                    public String toString() {
                        return actionName + " result";
                    }
                };
            }

            public String getActionName() {
                return actionName;
            }

            public String getDeviceId() {
                return devId;
            }

            public String getServiceName() {
                return serviceName;
            }

            public Throwable getFailure() {
                return fail;
            }

            public void setResultRecipient(ResultRecipient resultRecipient) {
                this.resultRecipient = resultRecipient;
            }

            public void setFailure(Throwable e) {
                fail = e;
            }

            @Override
            public String toString() {
                return getDeviceId() + ":" + getServiceName() + ":" + getActionName() + ":" + getResult();
            }
        }
    }

    static class Executor {

        Map<String, Device> devices = new HashMap<String, Device>() {
            {
                put("d2", new Device() {
                    final List<Runnable> jobs = new ArrayList<Runnable>();
                    final Thread runner = new Thread() {
                        public void run() {
                            while (true) {
                                ArrayList<Runnable> tmpJobs = new ArrayList<Runnable>();
                                synchronized (jobs) {
                                    try {
                                        jobs.wait();
                                    } catch (InterruptedException e) {
                                        continue;
                                    }
                                    tmpJobs.addAll(jobs);
                                    jobs.clear();
                                }

                                for (Runnable job : tmpJobs) {
                                    job.run();
                                }
                            }
                        }
                    };

                    {
                        runner.start();
                    }

                    public Service getService(String serviceName) {
                        return new Service() {
                            public Action getAction(String actionName) {
                                return new Action() {
                                    public void executeAsyncWith(final ActionContext ctx) {
                                        synchronized (jobs) {
                                            jobs.add(new Runnable() {
                                                public void run() {
                                                    System.out.println("Starting job on " + ctx);
                                                    try {
                                                        Thread.sleep(10000);
                                                    } catch (InterruptedException e) {
                                                        // ignore
                                                    }
                                                    System.out.println("Completed job on " + ctx);
                                                    ctx.sendResult();
                                                }
                                            });
                                            jobs.notifyAll();
                                        }
                                    }
                                };
                            }
                        };
                    }
                });
            }
        };
        private static final Device DEFAULT_DEVICE = new Device() {
            public Service getService(String serviceName) {
                return DEFAULT_SERVICE;
            }
        }; // returns default service
        private static final Service DEFAULT_SERVICE = new Service() {
            public Action getAction(String actionName) {
                return DEFAULT_ACTION;
            }
        };
        private static final Action DEFAULT_ACTION = new Action() {
            private int seq;

            /**
             * This method must be implemented by the adaptor. Either it -
             * executes several actions in parallel (e.g. UPnP), or - sends them
             * to queue for execution one at the time (e.g Z-wave)
             *
             * @param ctx
             */
            public void executeAsyncWith(final ActionContext ctx) {
                // Should set failure info
                new Thread("ExecutorThread-" + seq++) {
                    public void run() {
                        try {
                            System.out.println(this + " executes " + ctx);
                            ctx.sendResult();
                        } catch (Throwable e) {
                            ctx.setFailure(e);
                        }
                    }
                }.start();
            }
        };

        public void execute(Actions actions) {
            for (Iterator iterator = actions.getActionContexts().iterator(); iterator.hasNext();) {
                final ActionContext actionContext = (ActionContext) iterator.next();
                final Action action = getDevice(actionContext.getDeviceId()).
                        getService(actionContext.getServiceName()).
                        getAction(actionContext.getActionName());
                action.executeAsyncWith(actionContext);
            }
        }

        private Device getDevice(String device) {
            if (devices.containsKey(device)) {
                return devices.get(device);
            }
            return DEFAULT_DEVICE;
        }
    }

    static class WarpConnection implements Actions.ResultRecipient {

        Executor executor = new Executor();
        private boolean aggr;

        public WarpConnection(boolean aggr) {
            this.aggr = aggr;
        }

        public void onCompleted(List<? extends ActionContext> completedActions) {
            // Make JSON (marshal) ctx Send to warp
            System.out.println("Sending " + completedActions + " via warp...");
        }

        void receive(String data) {
            // parse data (unmarshal) into ActionContext
            Actions actions = Actions.SerializationSupport.deserialize(data);
            actions.aggregateResults(aggr);
            actions.setResultRecipient(this);
            executor.execute(actions);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WarpConnection warpConnection = new WarpConnection(true);
        warpConnection.receive("Kalle");

        Thread.sleep(30 * 1000);

        System.out.println("No aggr");
        warpConnection = new WarpConnection(false);
        warpConnection.receive("Kalle");

        Thread.sleep(30 * 1000);

        System.exit(0);
    }
}
