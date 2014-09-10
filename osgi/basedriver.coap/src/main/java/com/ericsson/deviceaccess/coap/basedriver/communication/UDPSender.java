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

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for sending out UDP using either unicast or multicast socket.
 */
public class UDPSender implements TransportLayerSender, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPSender.class);
    private final UDPTask POISON = new UDPTask(null);

    private MulticastSocket multicastSocket;

    private DatagramSocket socket;

    private BlockingQueue<UDPTask> queue;

    private volatile AtomicBoolean running = new AtomicBoolean(false);

    private Thread thread;

    /**
     * Constructor with multicast socket used for sending
     *
     * @param multicastSocket multicast socket to be used for sending
     */
    public UDPSender(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Constructor with unicast socket used for sending
     *
     * @param socket socket to be used for sending
     */
    public UDPSender(DatagramSocket socket) {
        this.socket = socket;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void start() {
        running.set(true);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        UDPTask task;
        while (running.get()) {
            try {
                task = queue.take();
            } catch (InterruptedException ex) {
                continue;
            }
            if (task == POISON) {
                break;
            }
            task.run();
        }

        if (multicastSocket != null) {
            multicastSocket.close();
        }
        if (socket != null) {
            socket.close();
        }
        queue = null;
        thread = null;
    }

    @Override
    public void sendMessage(CoAPMessage message) {
        queue.add(new UDPTask(message));
    }

    /**
     * Send given content to the given address
     *
     * @param content content to be sent
     * @param contentLength length of content to be sent
     * @param socketAddress address to where the content should be sent
     */
    public void send(byte[] content, int contentLength, SocketAddress socketAddress) {
        try {
            DatagramPacket outgoingDatagram = new DatagramPacket(content, content.length, socketAddress);
            if (socket != null) {
                socket.send(outgoingDatagram);
            } else {
                multicastSocket.send(outgoingDatagram);
            }
        } catch (IOException e) {
            LOGGER.warn("Couldn't send content.", e);
        }
    }

    /**
     * This method will stop the running threads and close the sockets.
     */
    @Override
    public void stopService() {
        running.set(false);
        queue.add(POISON);
    }

    protected class UDPTask implements Runnable {

        private final CoAPMessage message;

        protected UDPTask(CoAPMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            byte[] encoded = message.encoded();
            send(encoded, encoded.length, message.getSocketAddress());
        }
    }
}
