/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
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

  protected static List<Element> createElementList(Element element) {
    List<Element> elementList = new ArrayList<>();

    NodeList childNodes = element.getChildNodes();
    int numChildren = childNodes.getLength();

    for (int i = 0; i < numChildren; i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      elementList.add((Element)childNode);
    }

    return elementList;
  }

  protected static Map<String, String> createAttributeTable(Element e) {
    Map<String, String> attributeTable = new HashMap<>();
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
