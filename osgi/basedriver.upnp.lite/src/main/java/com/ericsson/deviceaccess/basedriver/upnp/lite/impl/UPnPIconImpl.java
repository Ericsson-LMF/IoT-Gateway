package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.osgi.service.upnp.UPnPIcon;

public class UPnPIconImpl implements UPnPIcon {
	String mimetype;
	String url;
	int width;
	int height;
	int depth;
	
	protected UPnPIconImpl(String mimetype, int width, int height, int depth, String url) {
		this.mimetype = mimetype;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getMimeType() {
		return mimetype;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getSize() {
		return 0;
	}

	public InputStream getInputStream() throws IOException {
		URL loc = new URL(url);
		return loc.openConnection().getInputStream();
	}
	
	public String toString() {
		return "UPnP icon: mimetype=" + mimetype + ", width=" + width + ", height=" + height + ", depth=" + depth + ", url=" + url;
	}
}
