/*
 * ComponentInstanciator.java
 *
 * Created on 13 juillet 2000, 09:50
 */

package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.MissingResourceException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class Simpleparser {
  private static ResourceLocator resources = null;
  private static String xmlPackage = "";
  // Init Function
  static {
    try {
      resources = new ResourceLocator(
          "com.stratelia.webactiv.beans.admin.instance.control.instanciator",
          "");
      xmlPackage = resources.getString("xmlPackage");

    } catch (MissingResourceException mre) {
      SilverTrace.error("admin", "Simpleparser.static",
          "admin.EX_ERR_SIMPLEPARSER_RESOURCES_NOT_FOUND", mre);
    }
  }

  public static void main(String argv[]) {
    if (argv.length != 1) {
      SilverTrace.info("admin", "Simpleparser.main",
          "admin.MSG_INFO_SIMPLEPARSER_USAGE");
      return;
    }

    try {
      DocumentBuilder domParser = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      Document doc = domParser.parse(new FileInputStream(xmlPackage + "/"
          + argv[0]));
      doc.getDocumentElement().normalize();
      WADOMUtil.printSubtree(new PrintWriter(System.out), WADOMUtil.findNode(
          doc, "Profiles"), WADOMUtil.findNode(doc, "Profiles"));
    } catch (SAXParseException err) {
      SilverTrace.error("admin", "Simpleparser.main",
          "admin.EX_ERR_PARSING_ERROR", "line " + err.getLineNumber()
              + ", uri " + err.getSystemId(), err);
    } catch (SAXException e) {
      SilverTrace.error("admin", "Simpleparser.main",
          "admin.EX_ERR_PARSING_ERROR", e);
    } catch (Throwable t) {
      SilverTrace.error("admin", "Simpleparser.main",
          "admin.EX_ERR_PARSING_ERROR", t);
    }
  }
}
