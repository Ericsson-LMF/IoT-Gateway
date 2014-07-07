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

package com.ericsson.deviceaccess.coap.basedriver.util;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPActivator;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This class is responsible for encoding a CoAPMessage (either request or
 * response) into a byte array
 */
public class CoAPMessageWriter implements CoAPMessageFormat {

	private CoAPMessage message;
	private ByteArrayOutputStream outputStream;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            message to be encoded
	 */
	public CoAPMessageWriter(CoAPMessage message) {
		this.message = message;
		this.outputStream = new ByteArrayOutputStream();
	}

	/**
	 * This method actually does the encoding.
	 * 
	 * @return encoded byte array
	 */
	public byte[] encode() {

		/*
			CoAPActivator.logger
					.debug("CoAPMessageWriter: encode a message with message ID "
							+ message.getIdentifier());
		*/
		// These are the header that all messages should have
		int version = message.getVersion();
		int messageType = message.getMessageType().getNo();
		int optionCount = message.getOptionCount();
		int messageCode = message.getCode();

		byte[] bytes = new byte[1];
		byte tmpByte = bytes[0];

		// First byte with version, message type & option count
		byte headerByte = BitOperations.setBitsInByte(tmpByte, VERSION_START,
				VERSION_LENGTH,
				BitOperations.getBitsInIntAsByte(version, 0, VERSION_LENGTH));
		headerByte = BitOperations.setBitsInByte(headerByte, TYPE_START,
				TYPE_LENGTH,
				BitOperations.getBitsInIntAsByte(messageType, 0, TYPE_LENGTH));
		headerByte = BitOperations.setBitsInByte(headerByte,
				OPTION_COUNT_START, OPTION_COUNT_LENGTH,
				BitOperations.getBitsInIntAsByte(optionCount, 0,
						OPTION_COUNT_LENGTH));

		outputStream.write(headerByte);

		bytes = new byte[1];
		byte tmpByteCode = bytes[0];

		// byte tmpByteCode = ByteBuffer.allocate(1).get();
		byte codeByte = BitOperations.setBitsInByte(tmpByteCode, CODE_START,
				CODE_LENGTH,
				BitOperations.getBitsInIntAsByte(messageCode, 0, CODE_LENGTH));

		outputStream.write(codeByte);
		
		// Message ID is a 16-bit unsigned => two bytes
		byte[] messageIdBytes = BitOperations.splitIntToBytes(message
				.getMessageId());
		outputStream.write(messageIdBytes[2]);
		outputStream.write(messageIdBytes[3]);

		if (optionCount > 0) {
			this.encodeOptionHeaders();
		}

		if (message.getPayload() != null && message.getPayload().length > 0) {
			try {
				outputStream.write(message.getPayload());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		byte[] byteArray = outputStream.toByteArray();

		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArray;
	}

	/**
	 * This method encodes the option headers that are part of the message
	 */
	private void encodeOptionHeaders() {

		// Sort options
		// TODO is there better to handle this??
		LinkedList options = message.getOptionHeaders();
		Collections.sort(options);

		// MultiMap options = message.getOptionHeaders();

		int previousOption = 0;
		int optionDelta = 0;

		Iterator it = options.iterator();

		// Go through different option numbers
		while (it.hasNext()) {
			CoAPOptionHeader header = (CoAPOptionHeader) it.next();

			int optionNumber = header.getOptionNumber();

			if ((previousOption % 14) == 0) {
				optionDelta = optionNumber;
			} else {
				optionDelta = optionNumber - previousOption;
			}

			// cache the previous option number so it can be used to detect
			// fenceposts
			previousOption = optionNumber;
			int optionLength = header.getLength();

			/*
				CoAPActivator.logger
						.debug("CoAPMessageWriter: encode option number "
								+ optionNumber);
			*/

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] bytes = new byte[3];
			byte tmpByte = bytes[0];
			byte optionByte2 = bytes[1];

			byte optionByte = BitOperations.setBitsInByte(tmpByte,
					OPTION_DELTA_START, OPTION_DELTA_LENGTH, BitOperations
							.getBitsInIntAsByte(optionDelta, 0,
									OPTION_DELTA_LENGTH));

			// if the length is < 15, do this
			if (header.isNormalLength()) {

				optionByte = BitOperations.setBitsInByte(optionByte,
						OPTION_LENGTH_START, OPTION_LENGTH_LENGTH,
						BitOperations.getBitsInIntAsByte(optionLength, 0,
								OPTION_LENGTH_LENGTH));
				outputStream.write(optionByte);

				// If length >= 15, do this
			} else {

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				optionByte = BitOperations.setBitsInByte(optionByte,
						OPTION_LENGTH_START, OPTION_LENGTH_LENGTH, 15);
				stream.write(optionByte);

				optionByte2 = BitOperations.setBitsInByte(optionByte2, 0, 8,
						(optionLength - 15));
				stream.write(optionByte2);
				try {
					outputStream.write(stream.toByteArray());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (header.getLength() > 0) {
				byte[] optionValueBytes = header.getValue();

				try {
					outputStream.write(optionValueBytes);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
