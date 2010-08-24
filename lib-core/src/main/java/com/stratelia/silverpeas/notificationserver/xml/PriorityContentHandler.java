/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.notificationserver.xml;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationTag;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ehugonnet
 */
public class PriorityContentHandler extends DefaultHandler {

  private NotificationData data;
  private ContentHandler parent;
  private XMLReader parser;

  public PriorityContentHandler(NotificationData data, ContentHandler parent, XMLReader parser) {
    this.data = data;
    this.parent = parent;
    this.parser = parser;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    switch (tag) {
      case PRIORITY: {
       data.setPrioritySpeed(attributes.getValue(NotificationTag.SPEED.toString()));
        break;
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    if (tag == NotificationTag.PRIORITY) {
      parser.setContentHandler(parent);
    }
  }
}
