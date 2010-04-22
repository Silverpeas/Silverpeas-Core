/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver;

import java.io.StringReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.xml.sax.InputSource;

import com.silverpeas.util.EncodeHelper;

public class NotificationServerUtil {
  public static String convertNotificationDataToXML(NotificationData p_Data)
      throws NotificationServerException {
    StringBuffer xml = new StringBuffer();

    if (p_Data != null) {
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      // xml.append( "<!DOCTYPE NOTIFY SYSTEM \"notification.dtd\">");
      xml.append("<NOTIFY>");
      xml.append("	<LOGIN>");
      xml.append("		<USER>"
          + EncodeHelper.javaStringToXmlString(p_Data.getLoginUser())
          + "</USER>");
      xml.append("		<PASSWORD>"
          + EncodeHelper.javaStringToXmlString(p_Data.getLoginPassword())
          + "</PASSWORD>");
      xml.append("	</LOGIN>");
      xml.append("	<MESSAGE>"
          + EncodeHelper.javaStringToXmlString(p_Data.getMessage())
          + "</MESSAGE>");
      xml.append("	<SENDER>");
      xml.append("		<ID>"
          + EncodeHelper.javaStringToXmlString(p_Data.getSenderId()) + "</ID>");
      xml.append("		<NAME>"
          + EncodeHelper.javaStringToXmlString(p_Data.getSenderName())
          + "</NAME>");
      xml.append("		<ANSWERALLOWED>" + p_Data.isAnswerAllowed()
          + "</ANSWERALLOWED>");
      xml.append("	</SENDER>");
      xml.append("	<COMMENT>"
          + EncodeHelper.javaStringToXmlString(p_Data.getComment())
          + "</COMMENT>");
      xml.append("	<TARGET CHANNEL=\""
          + EncodeHelper.javaStringToXmlString(p_Data.getTargetChannel())
          + "\">");
      xml.append("		<NAME>"
          + EncodeHelper.javaStringToXmlString(p_Data.getTargetName())
          + "</NAME>");
      xml.append("		<RECEIPT>"
          + EncodeHelper.javaStringToXmlString(p_Data.getTargetReceipt())
          + "</RECEIPT>");
      xml.append("		<PARAM>"
          + EncodeHelper.javaStringToXmlString(packKeyValues(p_Data
          .getTargetParam())) + "</PARAM>");
      xml.append("	</TARGET>");
      xml.append("	<PRIORITY SPEED=\""
          + EncodeHelper.javaStringToXmlString(p_Data.getPrioritySpeed())
          + "\"/>");
      xml.append("	<REPORT>");
      xml.append("	</REPORT>");
      xml.append("</NOTIFY>");
    }

    return xml.toString();
  }

  /**
	 * 
	 */
  public static NotificationData convertXMLToNotificationData(String p_XML)
      throws NotificationServerException {
    NotificationDataXML ndXML = new NotificationDataXML();
    InputSource is;

    is = new InputSource(new StringReader(p_XML));
    ndXML.ParseXML(is);

    return ndXML.getNotificationData();
  }

  /**
	 * 
	 */
  static public Hashtable unpackKeyValues(String keyvaluestring) {
    Hashtable result = new Hashtable();
    char c;
    StringBuffer key = new StringBuffer();
    StringBuffer value = new StringBuffer();
    StringBuffer sb;

    if (keyvaluestring != null) {
      sb = key;
      for (int i = 0; i < keyvaluestring.length(); i++) {
        c = keyvaluestring.charAt(i);
        if (c == ';') {
          if (((i + 1) < keyvaluestring.length())
              && (keyvaluestring.charAt(i + 1) == ';')) { // Two ; -> just take
            // one and append it
            // to the current
            // value
            sb.append(c);
            i++;
          } else { // the ; is the Key/Values pairs separator
            String strValue = value.toString();
            if (strValue.startsWith("#DATE#")) {
              strValue = strValue.substring(6, strValue.length());
              result.put(key.toString(), new Date(Long.valueOf(strValue)
                  .longValue()));
            } else {
              result.put(key.toString(), strValue);
            }

            key.setLength(0);
            value.setLength(0);
            sb = key;
          }
        } else if (c == '=') {
          if (((i + 1) < keyvaluestring.length())
              && (keyvaluestring.charAt(i + 1) == '=')) { // Two = -> just take
            // one and append it
            // to the current
            // value
            sb.append(c);
            i++;
          } else { // the = is the Key/Value separator
            sb = value;
          }
        } else {
          sb.append(c);
        }
      }
      String strValue = value.toString();
      if (strValue.startsWith("#DATE#")) {
        strValue = strValue.substring(6);
        result
            .put(key.toString(), new Date(Long.valueOf(strValue).longValue()));
      } else {
        result.put(key.toString(), strValue);
      }
    }
    return result;
  }

  /**
   * Method declaration
   * @param theValue
   * @return
   * @see
   */
  static protected String doubleSeparators(String theValue) {
    if (theValue == null) {
      return "";
    }
    StringBuffer sb = new StringBuffer(theValue.length() + 5);
    char c;
    int i;

    for (i = 0; i < theValue.length(); i++) {
      c = theValue.charAt(i);
      if (c == ';') {
        sb.append(";;");
      } else if (c == '=') {
        sb.append("==");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
	 * 
	 */
  static public String packKeyValues(Hashtable keyValues) {
    StringBuffer sb = new StringBuffer();
    Enumeration theKeys;
    String theKey;
    boolean bNotTheFirst = false;

    if (keyValues != null) {
      theKeys = keyValues.keys();
      while (theKeys.hasMoreElements()) {
        theKey = (String) theKeys.nextElement();
        if (bNotTheFirst) {
          sb.append(';');
        }

        String keyValue = "";
        boolean success = true;

        // try first to treat the value as a String
        try {
          keyValue = (String) keyValues.get(theKey);
        } catch (ClassCastException cce) {
          success = false;
        }

        // if first attempt failed, try to treat it as a Date
        if (!success) {
          try {
            Date date = (Date) keyValues.get(theKey);
            keyValue = "#DATE#" + String.valueOf(date.getTime());
            success = true;
          } catch (ClassCastException cce) {
            success = false;
          }
        }

        if (success) {
          sb
              .append(doubleSeparators(theKey) + "="
              + doubleSeparators(keyValue));
          bNotTheFirst = true;
        }
      }
    }
    return sb.toString();
  }

}