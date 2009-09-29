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