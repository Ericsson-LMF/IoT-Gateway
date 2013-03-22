/*********************************************************************
* � Copyright Ericsson AB 2010. All rights reserved.
*
*
*  The contents of this file are subject to revision without notice due to continued progress in
*  methodology, design and manufacturing. Ericsson shall have no liability for any error or
*  damage of any kind resulting from the use of this file.
*
*  ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY NATURE
*  WHATSOEVER (NEITHER EXPRESSED NOR IMPLIED) WITH RESPECT TO THE
*  SOFTWARE, INCLUDING BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, INTERPRETABILITY,
*  INTEROPERABILITY OR NON-INFRINGEMENT OF THIRD PARTY INTELLECTUAL
*  PROPERTY RIGHTS OR ANY OTHER PROPRIETARY RIGHTS OF A THIRD PARTY,
*  AND IN NO EVENT SHALL ERICSSON BE LIABLE FOR ANY DIRECT, INDIRECT,
*  INCIDENTAL AND OR CONSEQUENTIAL DAMAGES AND OR LOSS WHATSOEVER
*  (INCLUDING BUT NOT LIMITED TO MONETARY LOSSES OR LOSS OF DATA)
*  ARISING FROM ANY KIND OF USE OF 
*  THIS SOFTWARE AND/OR ITS DERIVATIVES.
*
*/

package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPnPUtils {
    private static final Logger log = LoggerFactory.getLogger(UPnPUtils.class);

    public static String escapeXml(String xml) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i<xml.length(); i++) {
            if ('<' == xml.charAt(i)) {
                sb.append("&lt;");
            }
            else if ('>' == xml.charAt(i)) {
                sb.append("&gt;");
            }
            else if ('"' == xml.charAt(i)) {
                sb.append("&quot;");
            }
            else if ('&' == xml.charAt(i)) {
                sb.append("&amp;");
            }
            else if ('\'' == xml.charAt(i)) {
                sb.append("&apos;");
            }
            else {
                sb.append(xml.charAt(i));
            }
        }
        return new String(sb);
    }

    public static String unEscapeXml(String xml) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i<xml.length(); i++) {
            if ('&' == xml.charAt(i)) {
                if(i < xml.length() - 3) {
                    if('l' == xml.charAt(i + 1) && 't' == xml.charAt(i + 2) && ';' == xml.charAt(i + 3)) {
                        sb.append('<');
                        i += 3;
                    } else if('g' == xml.charAt(i + 1) && 't' == xml.charAt(i + 2) && ';' == xml.charAt(i + 3)) {
                        sb.append('>');
                        i += 3;
                    } else if(i < xml.length() - 4) {
                        if('a' == xml.charAt(i + 1) && 'm' == xml.charAt(i + 2) && 'p' == xml.charAt(i + 3) && ';' == xml.charAt(i + 4)) {
                            sb.append('&');
                            i += 4;
                        } else if(i < xml.length() - 5) {
                            if('q' == xml.charAt(i + 1) && 'u' == xml.charAt(i + 2) && 'o' == xml.charAt(i + 3) && 't' == xml.charAt(i + 4) && ';' == xml.charAt(i + 5)) {
                                sb.append('"');
                                i += 5;
                            } else  if('a' == xml.charAt(i + 1) && 'p' == xml.charAt(i + 2) && 'o' == xml.charAt(i + 3) && 's' == xml.charAt(i + 4) && ';' == xml.charAt(i + 5)) {
                                sb.append('\'');
                                i += 6;
                            } else {
                                sb.append(xml.charAt(i));
                            }
                        } else {
                            sb.append(xml.charAt(i));
                        }
                    } else {
                        sb.append(xml.charAt(i));
                    }
                } else {
                    sb.append(xml.charAt(i));
                }
            } else {
                sb.append(xml.charAt(i));
            }
        }
        return new String(sb);
    }

  	public static Object parseString(String value,String upnpType) throws Exception{
		if (value ==null && upnpType.equals("string"))
                value = "";
        if((value==null)||(upnpType==null))
				throw new NullPointerException("Must be specified a valid value and upnpType");
		
		if (upnpType.equals("ui1") || upnpType.equals("ui2")
				|| upnpType.equals("i1") || upnpType.equals("i2")
				|| upnpType.equals("i4") || upnpType.equals("int")) {
			
			return new Integer(value);
		} else if (upnpType.equals("ui4")){			
			return new Long(value);
		} else if(upnpType.equals("time")){
			String[] timeFormats=new String[]{"HH:mm:ss"};
			Date d=getDateValue(value,timeFormats,timeFormats);
			
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			return new Long(
					c.get(Calendar.HOUR_OF_DAY)*3600000
					+c.get(Calendar.MINUTE)*60000
					+c.get(Calendar.SECOND)*1000
			);
		} else if(upnpType.equals("time.tz")) {
			String[] timeFormats=new String[]{"HH:mm:ssZ","HH:mm:ss"};
			Date d=getDateValue(value,timeFormats,timeFormats);
			TimeZone tz = TimeZone.getDefault();			
			Calendar c = Calendar.getInstance(tz);
			c.setTime(d);
			
			if(timeFormats[0].equals("HH:mm:ssZ")&&(tz.inDaylightTime(d)))
				c.add(Calendar.MILLISECOND,3600000);
			return new Long(
					c.get(Calendar.HOUR_OF_DAY)*3600000
					+c.get(Calendar.MINUTE)*60000
					+c.get(Calendar.SECOND)*1000					
			);
		} else if (upnpType.equals("r4") || upnpType.equals("float")) {				
			return new Float(value);
		} else if (upnpType.equals("r8") || upnpType.equals("number")
			|| upnpType.equals("fixed.14.4")){			
			return new Double(value);
		} else if (upnpType.equals("char")) {			
			return new Character(value.charAt(0));
		} else if (upnpType.equals("string") || upnpType.equals("uri")
				|| upnpType.equals("uuid")) {			
			return value;
		} else if (upnpType.equals("date")) {
			String[] timeFormats=new String[]{"yyyy-MM-dd"};
			
			Date d=getDateValue(value,timeFormats,timeFormats);
			return d;			
		} else if (upnpType.equals("dateTime")) {
			
			String[] timeFormats=new String[]{
					"yyyy-MM-dd",
					"yyyy-MM-dd'T'HH:mm:ss"
			};
			
			Date d=getDateValue(value,timeFormats,timeFormats);
			return d;
		} else if (upnpType.equals("dateTime.tz")) {
			
			String[] timeFormats=new String[]{
					"yyyy-MM-dd",
					"yyyy-MM-dd'T'HH:mm:ss",
					"yyyy-MM-dd'T'HH:mm:ssZ"
			};
			
			Date d=getDateValue(value,timeFormats,timeFormats);
			return d;			
		} else if (upnpType.equals("boolean")) {
			if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")
			|| value.equalsIgnoreCase("1"))
				return Boolean.TRUE;
			else
				return Boolean.FALSE;					
		} else if (upnpType.equals("bin.base64")) {
			return Base64.decode(value);
		} else if (upnpType.equals("bin.hex")) {
			return HexBin.decode(value);
		}
		throw new IllegalArgumentException("Invalid Binding");		
	}
	
	private static String normalizeTimeZone(String value){
		if(value.endsWith("Z")){
			value=value.substring(0,value.length()-1)+"+0000";
		}else if((value.length()>7)
			&&(value.charAt(value.length()-3)==':')
			&&((value.charAt(value.length()-6)=='-')||(value.charAt(value.length()-6)=='+'))){
			
			value=value.substring(0,value.length()-3)+value.substring(value.length()-2);
		}		
		return value;
	}
	
	private static Date getDateValue(String value, String[] timeFormats, String[] choosedIndex) throws ParseException {
		ParsePosition position = null;
		Date d;
		value=normalizeTimeZone(value);
		for (int i=0; i<timeFormats.length; i++) {
			position =  new ParsePosition(0);
			SimpleDateFormat  sdt = new SimpleDateFormat(timeFormats[i]);
			d=sdt.parse(value,position);
			if(d!=null){
				if(position.getIndex()>=value.length()){
					choosedIndex[0]=timeFormats[i];
					return d;			
				}
			}
		}
		throw new ParseException("Error parsing "+value,position.getIndex());
	}

}
