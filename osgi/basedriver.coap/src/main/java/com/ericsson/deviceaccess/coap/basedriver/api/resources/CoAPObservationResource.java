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
package com.ericsson.deviceaccess.coap.basedriver.api.resources;

import java.net.URI;
import java.util.Date;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPActivator;

/**
 * This is class for representing the resources related to observation
 * relationships.
 * 
 * A CoAPObservationResource has some additional attributes compared to a normal
 * CoAP resource. The attributes can be used for checking the validity of the
 * resource representations.
 * 
 */
public class CoAPObservationResource extends CoAPResource {

	private int latestValidObserve; // uint, use integer and lowest 16 bits
	private Date latestTimestamp;

	// unsigned int handled as long here
	private long maxOfe;

	public CoAPObservationResource(URI uri) {
		super(uri);
		this.latestTimestamp = null;
		this.latestValidObserve = 0;
		// max-ofe is defined by default to 0 in the draft
		this.maxOfe = 0;
	}

	public void setLatestValidObserveValue(short validObserve) {
		this.latestValidObserve = validObserve;
	}

	public int getLatestValidObserveValue() {
		return this.latestValidObserve;
	}

	public Date getLatestValidTimestamp() {
		return this.latestTimestamp;
	}

	public void setLatestValidTimestamp(Date timestamp) {
		this.latestTimestamp = timestamp;
	}

	/**
	 * Method to check if the received observation response is fresh or not
	 * @param presentObserve number of the observe request received
	 * @param date time of the observation
	 * @return boolean value indicating whether an observation with the parameter values is fresh or not
	 */
	public boolean isFresh(int presentObserve, Date date) {
		// draft-ietf-core-observe-03

		if (this.latestTimestamp == null && this.latestValidObserve == 0) {
			/*
				CoAPActivator.logger.debug("Initial observe notification");
			*/
			this.latestTimestamp = date;
			this.latestValidObserve = presentObserve;
			return true;
		}

		// compare the values given as parameters to the ones of this instance
		// based on the formula in draft-ietf-core-observe-03

		double val1 = (this.latestValidObserve - presentObserve)
				% Math.pow(2, 16);

		double exp15 = Math.pow(2, 15);
		boolean condition1 = val1 < exp15;

		double exp14 = Math.pow(2, 14);
		boolean condition2 = date.getTime() < (this.latestTimestamp.getTime() + exp14);

		if (condition1 && condition2) {
			/*
				CoAPActivator.logger.debug("Outdated notification, discard");
			*/
			return false;
		}

		/*
			CoAPActivator.logger.debug("Fresh notification");
		*/

		// Update the new values
		this.latestTimestamp = date;
		this.latestValidObserve = presentObserve;

		return true;

	}

	public void setMaxOfe(long maxOfe) {
		this.maxOfe = maxOfe;
	}

	public long getMaxOfe() {
		return this.maxOfe;
	}
}
