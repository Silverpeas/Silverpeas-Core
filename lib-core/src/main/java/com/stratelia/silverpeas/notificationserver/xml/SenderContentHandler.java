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
public class SenderContentHandler extends DefaultHandler {

  private NotificationData data;
  private ContentHandler parent;
  private XMLReader parser;

  public SenderContentHandler(NotificationData data, ContentHandler parent, XMLReader parser) {
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
      case NAME: {
        child = new SenderNameContentHandler(data, this, parser);
        break;
      }
      case ID: {
        child = new SenderIdContentHandler(data, this, parser);
        break;
      }
      case ANSWERALLOWED: {
        child = new AnswerAllowedContentHandler(data, this, parser);
        break;
      }
    }
    if(child != null) {
      parser.setContentHandler(child);
      child.startElement(uri, localName, qName, attributes);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    NotificationTag tag = NotificationTag.valueOf(qName);
    if (tag == NotificationTag.SENDER) {
      parser.setContentHandler(parent);
    }
  }
}
