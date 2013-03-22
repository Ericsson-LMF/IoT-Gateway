package com.ericsson.deviceaccess.upnp;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Vector;

import org.json.JSONArray;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.xmlpull.v1.XmlPullParserException;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.service.media.ContentDirectoryBase;

public class ContentDirectoryUPnPImpl extends ContentDirectoryBase {
    private UPnPDevice dev;

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
        args.put("StartingIndex", new Long(startIndex));
        args.put("RequestedCount", new Long(requestedCount));
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

    public SimpleBrowseResult executeSimpleBrowse(String id, int startingIndex,
    		int requestedCount, String sortCriteria) throws GenericDeviceException {
		Dictionary result;
		SimpleBrowseResult actionResult = new SimpleBrowseResult();
		try {
			result = UPnPUtil.browse(dev, getProperties(id, startingIndex, requestedCount, sortCriteria));
			Vector objects = DidlXmlPullParser.parseDidl((String)result.get("Result"));
			actionResult.Result = new JSONArray(objects).toString(); 
		} catch (UPnPException e) {
			throw new GenericDeviceException("Failed in invoking browse action" + e.getMessage());
		} catch (XmlPullParserException e) {
			throw new GenericDeviceException("Failed to parse DIDL document " + e.getMessage());
		} catch (IOException e) {
			throw new GenericDeviceException("Failed to parse DIDL document " + e.getMessage());
		}
		return actionResult;
	}
	
	private Properties getProperties(String id, int startingIndex,
    		int requestedCount, String sortCriteria){
		Properties props = new Properties();
		if(id == null || id.length() == 0){
			props.put("ObjectID", "0");
		} else {
			props.put("ObjectID", id);
		}
		
		props.put("StartingIndex", new Integer(startingIndex));
		props.put("RequestedCount", new Integer(requestedCount));
		props.put("SortCriteria", sortCriteria);
		
		return props;
	}
	

    protected void refreshProperties() {
        // NOP
    }



}
