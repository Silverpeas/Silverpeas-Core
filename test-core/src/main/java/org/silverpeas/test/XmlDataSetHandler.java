/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.test;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * An XML handler to parse a dataset from an XML file (according to a schema close to the DbUnit
 * dataset one) and to transform it into a SQL script.
 * </p>
 * In order the SQL statements in the script are well formed, please simple quote the string
 * (varchar) values in the XML dataset and take caution of the timestamp format. Example:
 * <pre><domainsp_group id="5000" superGroupId="[NULL]" name="'Springfield" description="Root group for Springfield'" /></pre>
 * @author mmoquillon
 */
public class XmlDataSetHandler extends DefaultHandler {

  private StringBuilder statements = new StringBuilder();

  public static String parseXmlDataSet(String xmlDataSet)
      throws IOException, SAXException, ParserConfigurationException {
    String sqlScript = "";
    InputStream inputStream = XmlDataSetHandler.class.getResourceAsStream(xmlDataSet);
    if (inputStream != null) {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      XmlDataSetHandler handler = new XmlDataSetHandler();
      parser.parse(inputStream, handler);
      sqlScript = handler.statements.toString();
      System.out.println(sqlScript);
    }

    return sqlScript;
  }


  private XmlDataSetHandler() {

  }

  @Override
  public void startElement(final String uri, final String localName, final String qName,
      final Attributes attributes) throws SAXException {
    if (attributes != null && attributes.getLength() > 0) {
      StringBuilder values = new StringBuilder().append(" VALUES (");
      statements.append("INSERT INTO ").append(qName).append(" (");
      for (int i = 0; i < attributes.getLength() - 1; i++) {
        statements.append(attributes.getQName(i)).append(", ");
        values.append("'").append(cast(attributes.getValue(i))).append("', ");
      }
      values.append("'")
          .append(cast(attributes.getValue(attributes.getLength() - 1)))
          .append("'); ");
      statements.append(attributes.getQName(attributes.getLength() - 1))
          .append(") ")
          .append(values.toString())
          .append("\n");
    }
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName)
      throws SAXException {
  }

  private String cast(final String value) {
    if (value.equals("[NULL]")) {
      return "NULL";
    } else {
      return value;
    }
  }
}
