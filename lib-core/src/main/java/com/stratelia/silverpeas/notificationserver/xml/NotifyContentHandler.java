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
public class NotifyContentHandler extends DefaultHandler {

  private NotificationData data;
  private XMLReader parser;

  public NotifyContentHandler(NotificationData data, XMLReader parser) {
    this.data = data;
    this.parser = parser;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    ContentHandler child = null;
    switch (tag) {
      case LOGIN: {
        child = new LoginContentHandler(data, this, parser);
        break;
      }
      case MESSAGE: {
        child = new MessageContentHandler(data, this, parser);
        break;
      }
      case SENDER: {
        child = new SenderContentHandler(data, this, parser);
        break;
      }
      case COMMENT: {
        child = new CommentContentHandler(data, this, parser);
        break;
      }
      case TARGET: {
        child = new TargetContentHandler(data, this, parser);
        break;
      }
      case PRIORITY: {
        child = new PriorityContentHandler(data, this, parser);
        break;
      }
    }
    if(child != null) {
      parser.setContentHandler(child);
      child.startElement(uri, localName, qName, attributes);
    }
  }
}
