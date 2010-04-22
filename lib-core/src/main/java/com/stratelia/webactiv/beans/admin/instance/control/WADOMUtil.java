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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class WADOMUtil {
  public static Document readDocument(String filename) throws IOException,
      SAXException, ParserConfigurationException {

    // if you are using the IBM parser, use the commented lines

    // Parser parser = new Parser(filename);
    // InputStream input = new FileInputStream(filename);
    // Document doc = parser.readStream(input);
    // input.close();
    // return doc;

    // if you are using the Sun parser, use these lines
    DocumentBuilder domParser = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder();
    Document doc = domParser.parse(new FileInputStream(filename));
    doc.getDocumentElement().normalize();
    return doc;

  }

  public static Node findNode(Node node, String name) {
    if (node.getNodeName().equals(name))
      return node;
    if (node.hasChildNodes()) {
      NodeList list = node.getChildNodes();
      int size = list.getLength();
      for (int i = 0; i < size; i++) {
        Node found = findNode(list.item(i), name);
        if (found != null)
          return found;
      }
    }
    return null;
  }

  public static String getNodeAttribute(Node node, String name) {
    if (node instanceof Element) {
      Element element = (Element) node;
      return element.getAttribute(name);
    }
    return null;
  }

  public static void printSubtree(PrintWriter writer, Node root, Node node) {
    if (node instanceof Element) {
      if (node != root) {
        writer.print("\n<" + node.getNodeName() + ">");
        SilverTrace.info("admin", "WADOMUtil.printSubtree",
            "msg.MSG_INFO_PRINT_SUBTREE", "<" + node.getNodeName() + ">");
      }
      if (node.hasChildNodes()) {
        NodeList list = node.getChildNodes();
        int size = list.getLength();
        for (int i = 0; i < size; i++) {
          printSubtree(writer, root, list.item(i));
        }
      }
      if (node != root) {
        writer.print("</" + node.getNodeName() + ">");
        SilverTrace.info("admin", "WADOMUtil.printSubtree",
            "msg.MSG_INFO_PRINT_SUBTREE", "<" + node.getNodeName() + ">");
      }
    } else if (node instanceof Text) {
      writer.print(node.getNodeValue().trim());
      SilverTrace.info("admin", "WADOMUtil.printSubtree",
          "msg.MSG_INFO_PRINT_SUBTREE", node.getNodeValue().trim());
    }
  }
}