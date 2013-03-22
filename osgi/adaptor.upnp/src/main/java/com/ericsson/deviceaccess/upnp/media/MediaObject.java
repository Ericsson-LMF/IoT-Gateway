package com.ericsson.deviceaccess.upnp.media;

import java.util.HashMap;

public class MediaObject extends HashMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String ID = "id";
	public static final String PARENT = "parent";
	public static final String NAME = "name";
	public static final String TYPE = "type";

	public static final String TYPE_DIRECTORY = "directory";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_AUDIO = "audio";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_UNKNOWN = "unknown";


	public void setId(String id) {
		put(ID, id);
	}
	public String getId() {
		return (String) get(ID);
	}
	public void setParent(String parent) {
		put(PARENT, parent);
	}
	public String getParent() {
		return (String) get(PARENT);
	}
	public void setName(String name) {
		put(NAME, name);
	}
	public String getName() {
		return (String) get(NAME);
	}
}
