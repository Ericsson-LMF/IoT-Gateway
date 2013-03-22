package com.ericsson.deviceaccess.spi.utility;

public class Utils {
	public static String replace(final String input, final String search, final String replace) {
		if (search.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}

		final StringBuffer result = new StringBuffer();

		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = input.indexOf(search, startIdx)) >= 0) {
			result.append(input.substring(startIdx, idxOld));
			result.append(replace);
			startIdx = idxOld + search.length();
		}

		result.append(input.substring(startIdx));
		return result.toString();
	}

	public static String escapeJSON(String input) {
		StringBuffer result = new StringBuffer();
		
		int len = input.length();
		for (int i = 0; i < len; i++) {
			char c = input.charAt(i);
			if (c == '\n') {
				result.append("\\n");
			} else if (c == '\r') {
				result.append("\\r");
			} else if (c == '\"') {
				result.append("\\\"");
			} else if (c == '\\') {
				result.append("\\\\");
			} else {
				result.append(c);
			}
		}
		
		return result.toString();
	}
}
