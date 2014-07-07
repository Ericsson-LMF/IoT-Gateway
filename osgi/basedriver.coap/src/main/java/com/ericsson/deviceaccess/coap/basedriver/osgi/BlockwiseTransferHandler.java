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
package com.ericsson.deviceaccess.coap.basedriver.osgi;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class takes care of handling blockwise transfers identified by Block1
 * and Block2 options. It will take care of creating the next block request to
 * be sent out etc, but LocalEndpoint class is responsible for sending out the
 * requests.
 * 
 * Note that blockwise message transfers towards observable resources do not
 * work at the moment.
 */
public class BlockwiseTransferHandler {

	private HashMap blockwiseMessages;
	private HashMap ongoingBlockwiseRequests;
	private LocalCoAPEndpoint endpoint;
	

	// Max block size from draft-ietf-core-blao
	// private static int maxBlockSize = 1024;

	private int maxBlockSize;
	private int maxSzx;

	/**
	 * Constructor
	 * 
	 * @param endpoint
	 *            endpoint sending the messages
	 */
	public BlockwiseTransferHandler(LocalCoAPEndpoint endpoint) {
		this.blockwiseMessages = new HashMap();
		this.ongoingBlockwiseRequests = new HashMap();
		this.endpoint = endpoint;
		this.maxSzx = 6;
		this.maxBlockSize = 1024; // blockwise transfer draft 07
	}

	/**
	 * This method is used to set the maximum value of szx to something else
	 * than 6
	 * 
	 * @param szx
	 *            maximum value of the szx (0<=szx<=6)
	 */
	public void setMaxSzx(int szx) {
		this.maxSzx = szx;
		Double blockSizeDouble = new Double(Math.pow(2, (this.maxSzx + 4)));
		this.maxBlockSize = blockSizeDouble.intValue();
	}

	/**
	 * This method is used when requests are sent blockwise. If the size of the
	 * request is > allowed block size defined in the local CoAP endpoint, this
	 * method will be called automatically to split the message.
	 * 
	 * @param request
	 *            request to be sent
	 * @param blockNumber
	 *            number of the block
	 * @throws CoAPException
	 */
	public CoAPRequest createBlockwiseRequest(CoAPRequest request,
			int blockNumber, int szx) throws CoAPException {
		if (request.getOptionHeaders(CoAPOptionName.BLOCK2).size() > 0) {
			return request;
		}

		else {
			CoAPRequest blockRequest = this.createBlock1Request(request,
					blockNumber, szx);
			return blockRequest;
		}
	}
	
	/**
	 * This method is used when responses are sent blockwise. If the size of the
	 * response is > allowed block size defined in the local CoAP endpoint, this
	 * method will be called automatically to split the message.
	 * 
	 * @param response
	 *            response to be sent
	 * @param blockNumber
	 *            number of the block
	 * @throws CoAPException
	 */
	public CoAPResponse createBlockwiseResponse(CoAPResponse response, int blockNumber, int szx) {
		
		// Control usage of Block1 option in CoAP response
		if (response.getOptionHeaders(CoAPOptionName.BLOCK1).size() > 0) {
			return response;
		}
		
		// Descriptive usage of Block2 option		
		return this.createBlock2Response(response, blockNumber, szx);
	}

	/**
	 * This method will be called when a message with option header Block2 is
	 * received. It will return the next block request that should be sent out,
	 * or null if the last block was received.
	 * 
	 * @param response
	 *            response with block2 option received
	 * @param request
	 *            original request
	 * @throws CoAPException
	 */
	public CoAPRequest block2OptionReceived(CoAPResponse response,
			CoAPRequest request) throws CoAPException {

		List block2 = response.getOptionHeaders(CoAPOptionName.BLOCK2);
		// TODO can there be multiple block options?
		CoAPOptionHeader blockOption = (CoAPOptionHeader) block2.get(0);

		// Check if there exist a message with the same token
		String tokenString = "";
		try {
			tokenString = new String(response.getTokenHeader().getValue(),
					"UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		CoAPMessage originalResponse = (CoAPMessage) this.blockwiseMessages
				.get(tokenString);

		if (originalResponse != null) {

			// append received payload to first response
			byte[] initialPayload = originalResponse.getPayload();
			byte[] newPayload = new byte[initialPayload.length
					+ response.getPayload().length];

			System.arraycopy(initialPayload, 0, newPayload, 0,
					initialPayload.length);
			System.arraycopy(response.getPayload(), 0, newPayload,
					initialPayload.length, response.getPayload().length);

			response.setPayload(newPayload);
		}

		blockwiseMessages.put(tokenString, response);

		BlockOptionHeader blockOptionHeader = new BlockOptionHeader(
				blockOption);

		// if the m flag is set, there are still more blocks
		if (blockOptionHeader.getMFlag()) {

			CoAPOptionHeader tokenHeader = response.getTokenHeader();
			CoAPRequest blockRequest = this.endpoint.createCoAPRequest(
					request.getMessageType(), 1, request.getSocketAddress(),
					request.getUriFromRequest(), tokenHeader);
			// blockRequest.setListener(request.getListener());
			BlockOptionHeader nextBlock;
			int nextBlockNumber = blockOptionHeader.getBlockNumber() + 1;
			int szx;
			// If the received szx is bigger that the client can accept, change
			// it to correct one for the next request
			if (blockOptionHeader.getSzx() > this.maxSzx) {
				szx = this.maxSzx;
			} else {
				szx = blockOptionHeader.getSzx();
			}

			// TODO do this in a more clever way
			nextBlock = new BlockOptionHeader(CoAPOptionName.BLOCK2,
					nextBlockNumber, false, szx);

			blockRequest.setListener(request.getListener());
			blockRequest.addOptionHeader(nextBlock);

			return blockRequest;
		} else {
			return null;
		}

	}

	/**
	 * This method will be called when a response with block1 option is received
	 * 
	 * @param response
	 *            received response
	 * @param request
	 *            request that was sent out
	 * @throws CoAPException
	 */
	public CoAPRequest block1OptionResponseReceived(CoAPResponse response,
			CoAPRequest request) throws CoAPException {
		// Block1 in a response means that the request is acknowledged
		// need to check if the size of block should be changed from the request

		// read the block size from the sent request
		CoAPOptionHeader blockOption = (CoAPOptionHeader) response
				.getOptionHeaders(CoAPOptionName.BLOCK1).get(0);
		BlockOptionHeader header = new BlockOptionHeader(blockOption);

		int szx = 6;
		// read szx from the response
		szx = header.getSzx();

		String tokenString = "";
		try {
			tokenString = new String(request.getTokenHeader().getValue(),
					"UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		CoAPRequest originalRequest = (CoAPRequest) this.ongoingBlockwiseRequests
				.get(tokenString);
		int diff = 1;
		int payloadHandled = 0;
		if (originalRequest.getOptionHeaders(CoAPOptionName.BLOCK1).size() > 0) {
			CoAPOptionHeader coapOption = (CoAPOptionHeader) (originalRequest
					.getOptionHeaders(CoAPOptionName.BLOCK1).get(0));
			BlockOptionHeader h = new BlockOptionHeader(coapOption);
			int originalSzx = h.getSzx();

			// TODO Can the client still adjust the size of the block after
			// block1??
			if (originalSzx > szx && (header.getBlockNumber() == 0)) {
				// recalculate the blocknumber

				// TODO use bit operations for the calculations..
				Double blockSizeDouble = new Double(Math.pow(2,
						(originalSzx + 4)));
				int blockSize = blockSizeDouble.intValue();

				blockSizeDouble = new Double(Math.pow(2, (szx + 4)));
				int newBlockSize = blockSizeDouble.intValue();
				diff = blockSize / newBlockSize;
			}
		}

		// if the szx has been decreased by the server, increase the block
		// number too!!!
		int newBlockNumber = header.getBlockNumber() + diff;

		// TODO mflag in the response indicates only if the response carries
		// the final response
		// if (header.getMFlag()) {
		CoAPRequest blockRequest = null;

		blockRequest = this.createBlock1Request(originalRequest,
				newBlockNumber, szx);

		// Calculate szx from the block size:
		// 2^(4+SZX) = 512 =>
		return blockRequest;
	}

	private CoAPRequest createBlock1Request(CoAPRequest request,
			int blockNumber, int szx) throws CoAPException {

		CoAPRequest blockRequest = null;
		int payloadLeft = 0;
		Double blockSizeDouble = new Double(Math.pow(2, (szx + 4)));
		int blockSize = blockSizeDouble.intValue();

		int payloadHandled = blockNumber * blockSize;

		if (request.getPayload() != null) {
			payloadLeft = request.getPayload().length - payloadHandled;
		}
		if (payloadLeft <= 0) {
			return blockRequest;
		}

		blockRequest = endpoint.createCoAPRequest(request.getMessageType(),
				request.getCode(), request.getSocketAddress(),
				request.getUriFromRequest(), request.getTokenHeader());

		blockRequest.setListener(request.getListener());

		byte[] payload = new byte[blockSize];
		System.arraycopy(request.getPayload(), payloadHandled, payload, 0,
				// blockSize); // will cause OutOfArrayIndex
				Math.min(payloadLeft, blockSize));

		blockRequest.setPayload(payload);

		// Check for more blocks, if the payload left is bigger than the
		// blocksize, then needs to be split
		boolean mFlag = blockSize < payloadLeft;
		if (mFlag) {
			// TODO should this be the blocksize or the payload left
			blockSize = payloadLeft;
		}

		// copy the options
		for (Iterator i = request.getOptionHeaders().iterator(); i.hasNext();) {
			CoAPOptionHeader optionHeader = (CoAPOptionHeader) i.next();

			if (!optionHeader.getOptionName().equals(
					CoAPOptionName.URI_HOST.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.URI_PATH.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.URI_PORT.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.TOKEN.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.BLOCK1.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.BLOCK2.getName())) {
				blockRequest.addOptionHeader(optionHeader);
			}
		}

		// Calculate szx from the block size:
		// 2^(4+SZX) = 512 =>

		// Form the block option header
		BlockOptionHeader nextBlock = new BlockOptionHeader(
				CoAPOptionName.BLOCK1, blockNumber, mFlag, szx);

		boolean ok = blockRequest.addOptionHeader(nextBlock);
		blockRequest.setListener(request.getListener());

		if (blockNumber == 0) {
			String tokenString = "";
			try {
				tokenString = new String(request.getTokenHeader().getValue(),
						"UTF8");
				this.ongoingBlockwiseRequests.put(tokenString, request);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return blockRequest;
	}
	
	// TODO: 
	//   This method is almost same as createBlock1Request() method.
	//   So common part should be cut off as an another method and be shared.
	//
	private CoAPResponse createBlock2Response(CoAPResponse response, int blockNumber, int szx) {
		CoAPResponse blockResponse = null;
		
		int payloadLeft = 0;
		Double blockSizeDouble = new Double(Math.pow(2, (szx + 4)));
		int blockSize = blockSizeDouble.intValue();

		int payloadHandled = blockNumber * blockSize;

		if (response.getPayload() != null) {
			payloadLeft = response.getPayload().length - payloadHandled;
		}
		// No need for blockwise transfer
		if (payloadLeft <= 0) {
			return response;
		}

		blockResponse = new CoAPResponse(1, response.getMessageType(),
				response.getCode(), response.getMessageId());
		blockResponse.setSocketAddress(response.getSocketAddress());

		//byte[] payload = new byte[blockSize];
		byte[] payload = new byte[Math.min(payloadLeft, blockSize)];
		System.arraycopy(response.getPayload(), payloadHandled, payload, 0,
				Math.min(payloadLeft, blockSize));

		blockResponse.setPayload(payload);

		// Check for more blocks, if the payload left is bigger than the
		// blocksize, then needs to be split
		boolean mFlag = blockSize < payloadLeft;
		if (mFlag) {
			// TODO should this be the blocksize or the payload left
			blockSize = payloadLeft;
		}

		// copy the options
		for (Iterator i = response.getOptionHeaders().iterator(); i.hasNext();) {
			CoAPOptionHeader optionHeader = (CoAPOptionHeader) i.next();

			if (/*!optionHeader.getOptionName().equals(
					CoAPOptionName.URI_HOST.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.URI_PATH.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.URI_PORT.getName())							
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.TOKEN.getName())							
					&& */!optionHeader.getOptionName().equals(
							CoAPOptionName.BLOCK1.getName())
					&& !optionHeader.getOptionName().equals(
							CoAPOptionName.BLOCK2.getName())) {
				blockResponse.addOptionHeader(optionHeader);
			}
		}

		// Calculate szx from the block size:
		// 2^(4+SZX) = 512 =>

		// Form the block option header
		BlockOptionHeader nextBlock = new BlockOptionHeader(
				CoAPOptionName.BLOCK2, blockNumber, mFlag, szx);

		boolean ok = blockResponse.addOptionHeader(nextBlock);

		/*
		if (blockNumber == 0) {
			String tokenString = "";
			try {
				tokenString = new String(response.getTokenHeader().getValue(), "UTF8");
				this.ongoingBlockwiseRequests.put(tokenString, response);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		*/
		
		return blockResponse;
	}
	
}
