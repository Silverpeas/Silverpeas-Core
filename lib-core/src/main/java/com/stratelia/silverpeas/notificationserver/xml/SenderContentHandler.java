/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    if (child != null) {
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
