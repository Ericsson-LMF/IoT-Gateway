package com.ericsson.deviceaccess.upnp.media;

public class MediaContainer extends MediaObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1481326050489710107L;
	

	public MediaContainer(){
		put(TYPE, "directory");
	}
	
	public String getType(){
		return "directory";
	}
	
	public void setType(){};
}
