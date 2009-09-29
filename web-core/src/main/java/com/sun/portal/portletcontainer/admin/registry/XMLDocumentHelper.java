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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.sun.portal.portletcontainer.admin.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XMLDocumentHelper is a Helper class to create DOM Elements and Attributes.
 */
public class XMLDocumentHelper {

  protected static Element getChildElement(Element element, String tagName) {
    NodeList childNodes = element.getChildNodes();
    int numChildren = childNodes.getLength();

    for (int i = 0; i < numChildren; i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element childElement = (Element) childNode;

      if (tagName != null) {
        String childTagName = childElement.getTagName();
        if (!childTagName.equals(tagName)) {
          continue;
        }
      }
      return childElement;
    }
    return null;
  }

  protected static List createElementList(Element element) {
    List elementList = new ArrayList();

    NodeList childNodes = element.getChildNodes();
    int numChildren = childNodes.getLength();

    for (int i = 0; i < numChildren; i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      elementList.add(childNode);
    }

    return elementList;
  }

  protected static Map createAttributeTable(Element e) {
    Map attributeTable = new HashMap();
    NamedNodeMap attrs = e.getAttributes();
    if (attrs != null) {
      int numAttrs = attrs.getLength();
      for (int i = 0; i < numAttrs; i++) {
        Node na = attrs.item(i);
        if (na.getNodeType() != Node.ATTRIBUTE_NODE) {
          continue;
        }
        Attr a = (Attr) na;
        attributeTable.put(a.getName(), a.getValue());
      }
    }
    return attributeTable;
  }

  protected static Element createElement(Document d, String tagName) {
    Element e = d.createElement(tagName);
    return e;
  }

}
