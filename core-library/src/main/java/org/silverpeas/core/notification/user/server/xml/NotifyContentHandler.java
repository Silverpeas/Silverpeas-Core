/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.notification.user.server.xml;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationTag;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
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
    if (child != null) {
      parser.setContentHandler(child);
      child.startElement(uri, localName, qName, attributes);
    }
  }
}
