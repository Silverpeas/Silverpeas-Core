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
public class TargetContentHandler extends DefaultHandler {

  private NotificationData data;
  private ContentHandler parent;
  private XMLReader parser;

  public TargetContentHandler(NotificationData data, ContentHandler parent, XMLReader parser) {
    this.data = data;
    this.parent = parent;
    this.parser = parser;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    ContentHandler child = null;
    switch (tag) {
      case TARGET: {
       data.setTargetChannel(attributes.getValue(NotificationTag.CHANNEL.toString()));
        break;
      }
      case NAME: {
        child = new TargetNameContentHandler(data, this, parser);
        parser.setContentHandler(child);
        break;
      }
      case RECEIPT: {
        child = new TargetReceiptContentHandler(data, this, parser);
        parser.setContentHandler(child);
        break;
      }
       case PARAM: {
        child = new TargetParamContentHandler(data, this, parser);
        parser.setContentHandler(child);
        break;
      }
    }
    if(child != null) {
      parser.setContentHandler(child);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    if (tag == NotificationTag.TARGET) {
      parser.setContentHandler(parent);
    }
  }
}
