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
