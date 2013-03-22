package com.ericsson.deviceaccess.upnp.media;

public class MediaItem extends MediaObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String URL = "url";

	public void setType(String type) {
		put(TYPE, type);
	}

	public String getType() {
		return (String) get(TYPE);
	}

	public void setUrl(String url) {
		put(URL, url);
	}

	public String getUrl() {
		return (String) get(URL);
	}
	
}
