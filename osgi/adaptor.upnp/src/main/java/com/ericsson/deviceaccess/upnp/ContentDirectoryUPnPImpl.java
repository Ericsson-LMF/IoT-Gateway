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
package com.ericsson.deviceaccess.upnp;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.service.media.ContentDirectoryBase;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Vector;
import org.json.JSONArray;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.xmlpull.v1.XmlPullParserException;

public class ContentDirectoryUPnPImpl extends ContentDirectoryBase {

    private final UPnPDevice dev;

    public ContentDirectoryUPnPImpl(UPnPDevice dev) {
        this.dev = dev;

        // this.putAction(new UPnPBrowse()); // Adds native upnp browse action
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public BrowseResult executeBrowse(String objectId, String browseFlag, int startIndex, int requestedCount, String sortCriteria, String filter) throws GenericDeviceException {
        Properties args = new Properties();
        args.put("ObjectID", objectId);
        args.put("BrowseFlag", browseFlag);
        args.put("Filter", filter);
        args.put("StartingIndex", (long) startIndex);
        args.put("RequestedCount", (long) requestedCount);
        args.put("SortCriteria", sortCriteria);
        try {
            Dictionary result = UPnPUtil.browse(dev, args);
            BrowseResult browseResult = new BrowseResult();
            browseResult.DidlDocument = (String) result.get("Result");
            browseResult.NumberReturned = ((Long) result.get("NumberReturned")).intValue();
            browseResult.TotalMatches = ((Long) result.get("TotalMatches")).intValue();
            browseResult.UpdateID = ((Long) result.get("UpdateID")).intValue();
            return browseResult;
        } catch (UPnPException e) {
            throw new GenericDeviceException("Failed in invoking browse action" + e.getMessage());
        }
    }

    @Override
    public SimpleBrowseResult executeSimpleBrowse(String id, int startingIndex,
            int requestedCount, String sortCriteria) throws GenericDeviceException {
        Dictionary result;
        SimpleBrowseResult actionResult = new SimpleBrowseResult();
        try {
            result = UPnPUtil.browse(dev, getProperties(id, startingIndex, requestedCount, sortCriteria));
            Vector objects = DidlXmlPullParser.parseDidl((String) result.get("Result"));
            actionResult.Result = new JSONArray(objects).toString();
        } catch (UPnPException e) {
            throw new GenericDeviceException("Failed in invoking browse action" + e.getMessage());
        } catch (XmlPullParserException | IOException e) {
            throw new GenericDeviceException("Failed to parse DIDL document " + e.getMessage());
        }
        return actionResult;
    }

    private Properties getProperties(String id, int startingIndex,
            int requestedCount, String sortCriteria) {
        Properties props = new Properties();
        if (id == null || id.isEmpty()) {
            props.put("ObjectID", "0");
        } else {
            props.put("ObjectID", id);
        }

        props.put("StartingIndex", startingIndex);
        props.put("RequestedCount", requestedCount);
        props.put("SortCriteria", sortCriteria);

        return props;
    }

    @Override
    protected void refreshProperties() {
        // NOP
    }

}
