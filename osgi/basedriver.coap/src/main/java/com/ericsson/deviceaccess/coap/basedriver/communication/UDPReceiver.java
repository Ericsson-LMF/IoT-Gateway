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
package com.ericsson.deviceaccess.coap.basedriver.communication;

import com.ericsson.deviceaccess.coap.basedriver.osgi.IncomingMessageListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents the UDP thread for listening to incoming UDP
 */
public class UDPReceiver implements Runnable, TransportLayerReceiver {

    // IP MTU is suggested to 1280 bytes in draft-ieft-core-coap-08, 1152 for
    // the message size, 1024 for the payload size
    private static int BUFFER_SIZE = 1280;

    final private DatagramSocket socket;
    final private MulticastSocket multicastSocket;
    final private List<IncomingMessageListener> coapListeners = Collections.synchronizedList(new ArrayList<>());
    Thread thread;

    private volatile AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructor using DatagramSocket
     *
     * @param socket
     */
    public UDPReceiver(DatagramSocket socket) {
        //CoAPActivator.logger.debug("UDP receiver with datagram socket");
        this.socket = socket;
        this.multicastSocket = null;
    }

    /**
     * Constructor using MulticastSocket
     *
     * @param multicastSocket
     */
    public UDPReceiver(MulticastSocket multicastSocket) {
        //CoAPActivator.logger.debug("UDP receiver with multicast socket");
        this.multicastSocket = multicastSocket;
        this.socket = null;
    }

    public void cleanup() {
        running.set(false);
        coapListeners.clear();

        if (thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                // TODO allocate buffer properly
                // allocate buffer
                byte[] buffer = new byte[BUFFER_SIZE + 1];
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

                if (multicastSocket != null) {
                    multicastSocket.receive(datagram);
                } else {
                    if (socket.isClosed()) {
                        System.out.println("Socket has been already closed!!");
                    }
                    socket.receive(datagram);
                }

                // check if receive is interrupted
                //
                // socket.close() will interrupt socket.receive()
                //
                if (socket != null && socket.isClosed()) {
                    running.set(false);
                    continue;
                }
                if (multicastSocket != null && multicastSocket.isClosed()) {
                    running.set(false);
                    continue;
                }
                if (thread.isInterrupted()) {
                    running.set(false);
                    continue;
                }

                /*
                 CoAPActivator.logger.debug("UDP Datagram received");
                 */
                this.datagramReceived(datagram);

            } catch (SocketException e) {
                if (socket != null && !socket.isClosed() || multicastSocket != null && !multicastSocket.isClosed()) {
                    e.printStackTrace();
                }
                running.set(false);
            } catch (IOException e) {
                // TODO error handling!
                if (running.get()) {
                    e.printStackTrace();
                }
                running.set(false);
            } catch (Exception e) {
                running.set(false);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        running.set(true);
        thread = new Thread(this);
        thread.start();
    }

    /**
     * This method is used for notifying the CoAP listeners when a UDP datagram
     * is received
     *
     * @param datagram received datagram
     */
    private void datagramReceived(DatagramPacket datagram) {
        coapListeners.forEach(listener -> listener.messageReceived(datagram));
    }

    /**
     * Add an incoming message listener
     *
     * @param listener listener to add
     */
    @Override
    public void addListener(IncomingMessageListener listener) {
        coapListeners.add(listener);
    }

    /**
     * Removes an incomingmessagelistener
     *
     * @param listener
     * @return Returns true if the list contained the specified element
     */
    @Override
    public boolean removeListener(IncomingMessageListener listener) {
        return coapListeners.remove(listener);
    }

    @Override
    public void stopService() {
        cleanup();
    }
}
