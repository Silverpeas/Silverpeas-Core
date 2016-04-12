/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.user.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.silverpeas.core.util.Charsets;

import org.silverpeas.core.notification.user.server.xml.NotifyContentHandler;
import org.silverpeas.core.exception.SilverpeasException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NotificationServerUtil {

  private static final SAXParserFactory parserFactory;

  static {
    parserFactory = SAXParserFactory.newInstance();
    parserFactory.setNamespaceAware(false);
    parserFactory.setValidating(false);
  }

  public static String convertNotificationDataToXML(NotificationData p_Data) {
    StringBuilder xml = new StringBuilder();

    if (p_Data != null) {
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      xml.append("<NOTIFY>");
      xml.append("	<LOGIN>");
      xml.append("		<USER><![CDATA[");
      xml.append(p_Data.getLoginUser());
      xml.append("]]></USER>");
      xml.append("		<PASSWORD><![CDATA[");
      xml.append(p_Data.getLoginPassword());
      xml.append("]]></PASSWORD>");
      xml.append("	</LOGIN>");
      xml.append("	<MESSAGE><![CDATA[");
      xml.append(p_Data.getMessage());
      xml.append("]]></MESSAGE>");
      xml.append("	<SENDER>");
      xml.append("		<ID><![CDATA[");
      xml.append(p_Data.getSenderId());
      xml.append("]]></ID>");
      xml.append("		<NAME><![CDATA[");
      xml.append(p_Data.getSenderName());
      xml.append("]]></NAME>");
      xml.append("		<ANSWERALLOWED>");
      xml.append(p_Data.isAnswerAllowed());
      xml.append("</ANSWERALLOWED>");
      xml.append("	</SENDER>");
      xml.append("	<COMMENT><![CDATA[");
      xml.append(p_Data.getComment());
      xml.append("]]></COMMENT>");
      xml.append("	<TARGET CHANNEL=\"");
      xml.append(p_Data.getTargetChannel());
      xml.append("\">");
      xml.append("		<NAME><![CDATA[");
      xml.append(p_Data.getTargetName());
      xml.append("]]></NAME>");
      xml.append("		<RECEIPT><![CDATA[");
      xml.append(p_Data.getTargetReceipt());
      xml.append("]]></RECEIPT>");
      xml.append("		<PARAM><![CDATA[");
      xml.append(packKeyValues(p_Data.getTargetParam()));
      xml.append("]]></PARAM>");
      xml.append("	</TARGET>");
      xml.append("	<PRIORITY SPEED=\"");
      xml.append(p_Data.getPrioritySpeed());
      xml.append("\"/>");
      xml.append("	<REPORT>");
      xml.append("	</REPORT>");
      xml.append("</NOTIFY>");
    }

    return xml.toString();
  }

  /**
   * @param p_XML
   * @return
   * @throws NotificationServerException
   */
  public static NotificationData convertXMLToNotificationData(String p_XML)
      throws NotificationServerException {
    NotificationData data = new NotificationData();
    InputStream xml = new ByteArrayInputStream(p_XML.getBytes(Charsets.UTF_8));
    try {
      SAXParser parser = parserFactory.newSAXParser();
      DefaultHandler handler = new NotifyContentHandler(data, parser.getXMLReader());
      parser.parse(xml, handler);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new NotificationServerException("NotificationDataXML()",
          SilverpeasException.ERROR,
          "notificationServer.EX_ERROR_IN_XML_PARSING", e);
    }
    return data;
  }

  /**
   * @param keyvaluestring
   * @return
   */
  static public Map<String, Object> unpackKeyValues(String keyvaluestring) {
    Map<String, Object> result = new HashMap<>();
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
              result.put(key.toString(), new Date(Long.valueOf(strValue)));
            } else if (strValue.startsWith("#BOOLEAN#")) {
              strValue = strValue.substring(9, strValue.length());
              result.put(key.toString(), new Boolean(strValue));
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
        result.put(key.toString(), new Date(Long.valueOf(strValue)));
      } else if (strValue.startsWith("#BOOLEAN#")) {
        strValue = strValue.substring(9, strValue.length());
        result.put(key.toString(), new Boolean(strValue));
      } else {
        result.put(key.toString(), strValue);
      }
    }
    return result;
  }

  /**
   * @param theValue
   * @return
   */
  static protected String doubleSeparators(String theValue) {
    if (theValue == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(theValue.length() + 5);
    int i;
    char[] chars = theValue.toCharArray();
    for (i = 0; i < chars.length; i++) {
      char c = chars[i];
      switch (c) {
        case '=': {
          sb.append("==");
          break;
        }
        case ';': {
          sb.append(";;");
          break;
        }
        default: {
          sb.append(c);
          break;
        }
      }
    }
    return sb.toString();
  }

  /**
   * @param keyValues
   * @return
   */
  static public String packKeyValues(Map<String, Object> keyValues) {
    StringBuilder sb = new StringBuilder();
    boolean bNotTheFirst = false;

    if (keyValues != null) {
      Set<String> theKeys = keyValues.keySet();
      for (String theKey : theKeys) {
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

        //then, try to treat it as a Boolean
        if (!success) {
          try {
            Boolean bool = (Boolean) keyValues.get(theKey);
            keyValue = "#BOOLEAN#" + String.valueOf(bool);
            success = true;
          } catch (ClassCastException cce) {
            success = false;
          }
        }

        if (success) {
          sb.append(doubleSeparators(theKey)).append('=').append(doubleSeparators(keyValue));
          bNotTheFirst = true;
        }
      }
    }
    return sb.toString();
  }
}
