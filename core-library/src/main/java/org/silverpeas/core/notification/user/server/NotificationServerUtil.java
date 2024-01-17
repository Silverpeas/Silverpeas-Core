/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server;

import org.silverpeas.core.notification.user.server.xml.NotifyContentHandler;
import org.silverpeas.core.util.Charsets;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotificationServerUtil {

  private static final SAXParserFactory parserFactory;
  private static final String DATE_TYPEID = "#DATE#";
  private static final String BOOLEAN_TYPEID = "#BOOLEAN#";
  private static final String LIST_TYPEID = "#LIST#";

  static {
    parserFactory = SAXParserFactory.newInstance();
    parserFactory.setNamespaceAware(false);
    parserFactory.setValidating(false);
  }

  private NotificationServerUtil() {

  }

  public static String convertNotificationDataToXML(NotificationData data) {
    StringBuilder xml = new StringBuilder();

    if (data != null) {
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      xml.append("<NOTIFY>");
      xml.append("  <LOGIN>");
      xml.append("    <USER><![CDATA[");
      xml.append(data.getLoginUser());
      xml.append("]]></USER>");
      xml.append("    <PASSWORD><![CDATA[");
      xml.append(data.getLoginPassword());
      xml.append("]]></PASSWORD>");
      xml.append("  </LOGIN>");
      xml.append("  <MESSAGE><![CDATA[");
      xml.append(data.getMessage());
      xml.append("]]></MESSAGE>");
      xml.append("  <SENDER>");
      xml.append("    <ID><![CDATA[");
      xml.append(data.getSenderId());
      xml.append("]]></ID>");
      xml.append("    <NAME><![CDATA[");
      xml.append(data.getSenderName());
      xml.append("]]></NAME>");
      xml.append("    <ANSWERALLOWED>");
      xml.append(data.isAnswerAllowed());
      xml.append("</ANSWERALLOWED>");
      xml.append("  </SENDER>");
      xml.append("  <COMMENT><![CDATA[");
      xml.append(data.getComment());
      xml.append("]]></COMMENT>");
      xml.append("  <TARGET CHANNEL=\"");
      xml.append(data.getTargetChannel());
      xml.append("\">");
      xml.append("    <NAME><![CDATA[");
      xml.append(data.getTargetName());
      xml.append("]]></NAME>");
      xml.append("    <RECEIPT><![CDATA[");
      xml.append(data.getTargetReceipt());
      xml.append("]]></RECEIPT>");
      xml.append("    <PARAM><![CDATA[");
      xml.append(packKeyValues(data.getTargetParam()));
      xml.append("]]></PARAM>");
      xml.append("  </TARGET>");
      xml.append("  <PRIORITY SPEED=\"");
      xml.append(data.getPrioritySpeed());
      xml.append("\"/>");
      xml.append("  <REPORT>");
      xml.append("  </REPORT>");
      xml.append("</NOTIFY>");
    }

    return xml.toString();
  }

  public static NotificationData convertXMLToNotificationData(String xml)
      throws NotificationServerException {
    NotificationData data = new NotificationData();
    InputStream input = new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8));
    try {
      SAXParser parser = parserFactory.newSAXParser();
      DefaultHandler handler = new NotifyContentHandler(data, parser.getXMLReader());
      parser.parse(input, handler);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new NotificationServerException(e);
    }
    return data;
  }

  public static Map<String, Object> unpackKeyValues(String keyValueString) {
    Map<String, Object> result = new HashMap<>();
    StringBuilder key = new StringBuilder();
    StringBuilder value = new StringBuilder();

    if (keyValueString != null) {
      parseKeyValueString(keyValueString, result, key, value);

      decodeValue(result, key, value);
    }
    return result;
  }

  private static void parseKeyValueString(final String keyValueString,
      final Map<String, Object> result, final StringBuilder key, final StringBuilder value) {
    char c;
    StringBuilder sb = key;
    int i = 0;
    while (i < keyValueString.length()) {
      c = keyValueString.charAt(i);
      if (c == ';') {
        if (((i + 1) < keyValueString.length()) &&
            (keyValueString.charAt(i + 1) == ';')) { // Two ; -> just take
          // one and append it
          // to the current
          // value
          sb.append(c);
          i++;
        } else { // the ; is the Key/Values pairs separator
          decodeValue(result, key, value);
          key.setLength(0);
          value.setLength(0);
          sb = key;
        }
      } else if (c == '=') {
        if (((i + 1) < keyValueString.length()) &&
            (keyValueString.charAt(i + 1) == '=')) { // Two = -> just take
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
      i++;
    }
  }

  private static void decodeValue(final Map<String, Object> result, final StringBuilder key,
      final StringBuilder value) {
    String strValue = value.toString();
    if (strValue.startsWith(DATE_TYPEID)) {
      strValue = strValue.substring(6);
      result.put(key.toString(), new Date(Long.parseLong(strValue)));
    } else if (strValue.startsWith(BOOLEAN_TYPEID)) {
      strValue = strValue.substring(9);
      result.put(key.toString(), Boolean.valueOf(strValue));
    } else if (strValue.startsWith(LIST_TYPEID)) {
      strValue = strValue.substring(6);
      List<String> listValue = Stream.of(strValue.split(",")).collect(Collectors.toList());
      result.put(key.toString(), listValue);
    } else {
      result.put(key.toString(), strValue);
    }
  }

  protected static String doubleSeparators(String theValue) {
    if (theValue == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(theValue.length() + 5);
    int i;
    char[] chars = theValue.toCharArray();
    for (i = 0; i < chars.length; i++) {
      char c = chars[i];
      switch (c) {
        case '=':
          sb.append("==");
          break;
        case ';':
          sb.append(";;");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  public static String packKeyValues(Map<String, Object> keyValues) {
    StringBuilder sb = new StringBuilder();
    boolean bNotTheFirst = false;

    if (keyValues != null) {
      Set<String> theKeys = keyValues.keySet();
      for (String theKey : theKeys) {
        if (bNotTheFirst) {
          sb.append(';');
        }

        bNotTheFirst = formatInString(theKey, keyValues, sb, bNotTheFirst);
      }
    }
    return sb.toString();
  }

  private static boolean formatInString(final String theKey, final Map<String, Object> keyValues,
      final StringBuilder sb, boolean bNotTheFirst) {
    String keyValue = "";
    boolean success = true;
    boolean result = bNotTheFirst;

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
        keyValue = DATE_TYPEID + date.getTime();
        success = true;
      } catch (ClassCastException cce) {
        success = false;
      }
    }

    //then, try to treat it as a Boolean
    if (!success) {
      try {
        Boolean bool = (Boolean) keyValues.get(theKey);
        keyValue = BOOLEAN_TYPEID + bool;
        success = true;
      } catch (ClassCastException cce) {
        success = false;
      }
    }

    if (!success) {
      try {
        Collection<String> collection = (Collection<String>) keyValues.get(theKey);
        if (!collection.isEmpty()) {
          keyValue = LIST_TYPEID + collection.stream().collect(Collectors.joining(","));
          success = true;
        }
      } catch (ClassCastException cce) {
        success = false;
      }
    }

    if (success) {
      sb.append(doubleSeparators(theKey)).append('=').append(doubleSeparators(keyValue));
      result = true;
    }
    return result;
  }
}
