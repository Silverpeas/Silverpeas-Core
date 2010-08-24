/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.stratelia.silverpeas.notificationserver.xml;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationTag;
import java.io.CharArrayWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ehugonnet
 */
class UserContentHandler extends DefaultHandler {

  private NotificationData data;
  private ContentHandler parent;
  private XMLReader parser;
  private CharArrayWriter buffer = new CharArrayWriter();

  public UserContentHandler(NotificationData data, ContentHandler parent, XMLReader parser) {
    this.data = data;
    this.parent = parent;
    this.parser = parser;
  }

   @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    buffer.write(ch, start, length);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    if (tag == NotificationTag.USER) {
      data.setLoginUser(buffer.toString());
      parser.setContentHandler(parent);
    }
  }
}