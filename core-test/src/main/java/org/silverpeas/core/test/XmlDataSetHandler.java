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

package org.silverpeas.core.test;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * An XML handler to parse a dataset from an XML file (according to a schema close to the DbUnit
 * dataset one) and to transform it into a SQL script.
 * </p>
 * Type of values are automatically guessed, so there is no need to convert some values on XML
 * data set.
 * @author mmoquillon
 */
public class XmlDataSetHandler extends DefaultHandler {

  private StringBuilder statements = new StringBuilder();
  private ConConf conConf = null;

  /**
   * Parses the given XML data set in order to transform it into a SQL insert script.
   * The tool tries to connect to a database from default values of {@link ConConf}.
   * @param xmlDataSet the absolute path of the XML data set to parse.
   * @return the SQL insert script that corresponds to the given XML data setreturn
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public static String parseXmlDataSet(String xmlDataSet)
      throws IOException, SAXException, ParserConfigurationException {
    return parseXmlDataSet(xmlDataSet, ConConf.database());
  }

  /**
   * Parses the given XML data set in order to transform it into a SQL insert script.
   * The tool tries to connect to a database from values of given {@link ConConf}.
   * @param xmlDataSet the absolute path of the XML data set to parse.
   * @param conConf the configuration in order to open a database connection.
   * @return the SQL insert script that corresponds to the given XML data set
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public static String parseXmlDataSet(String xmlDataSet, ConConf conConf)
      throws IOException, SAXException, ParserConfigurationException {
    String sqlScript = "";
    InputStream inputStream = XmlDataSetHandler.class.getResourceAsStream(xmlDataSet);
    if (inputStream != null) {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      XmlDataSetHandler handler = new XmlDataSetHandler();
      handler.conConf = conConf;
      parser.parse(inputStream, handler);
      sqlScript = handler.statements.toString();
      System.out.println(sqlScript);
    }

    return sqlScript;
  }

  /**
   * Hidden constructor.
   */
  private XmlDataSetHandler() {
  }

  /**
   * Gets the mapping between column names (lowercase) and the java type (so a class) of a given
   * table represented by its name.
   * @param table the name of the aimed table.
   * @return mapping between column names (lowercase) and the java type (so a class).
   */
  private Map<String, String> getTableColumnType(String table) {
    Map<String, String> tableColumns = new HashMap<>();
    try (Connection connection = DriverManager
        .getConnection(conConf.getUrl(), conConf.getUser(), conConf.getPassword())) {
      try (PreparedStatement statement = connection.prepareStatement("select * from " + table)) {
        try (ResultSet resultSet = statement.executeQuery()) {
          ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
          for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            tableColumns.put(resultSetMetaData.getColumnName(i).toLowerCase(),
                resultSetMetaData.getColumnClassName(i));
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tableColumns;
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName,
      final Attributes attributes) throws SAXException {
    if (attributes != null && attributes.getLength() > 0) {
      Map<String, String> tableColumnTypes = getTableColumnType(qName);
      StringBuilder values = new StringBuilder().append(" VALUES (");
      statements.append("INSERT INTO ").append(qName).append(" (");
      for (int i = 0; i < attributes.getLength() - 1; i++) {
        String type = tableColumnTypes.get(attributes.getQName(i).toLowerCase());
        statements.append(attributes.getQName(i)).append(", ");
        values.append(cast(attributes.getValue(i), type)).append(", ");
      }
      String type =
          tableColumnTypes.get(attributes.getQName(attributes.getLength() - 1).toLowerCase());
      values.append(cast(attributes.getValue(attributes.getLength() - 1), type)).append("); ");
      statements.append(attributes.getQName(attributes.getLength() - 1)).append(") ")
          .append(values.toString()).append("\n");
    }
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName)
      throws SAXException {
  }

  /**
   * Casts the value from the XML data set according to the type of the columns into database.
   * @param value the value from the XML data set.
   * @param type the type of the table column into the database.
   * @return the java type as string: {@link Class#getName()}
   */
  private String cast(final String value, final String type) {
    if (value.equals("[NULL]")) {
      return "NULL";
    } else {
      switch (type) {
        case "java.lang.String":
        case "java.util.Date":
        case "java.sql.Date":
        case "java.sql.Timestamp":
          return "'" + value + "'";
        default:
          return value;
      }
    }
  }

  /**
   * Configuration class to perform a database connection.
   */
  public static class ConConf {
    private String url = "jdbc:postgresql://localhost:5432/SilverpeasV5";
    private String user = "postgres";
    private String password = "postgres";

    public static ConConf database() {
      return new ConConf(null);
    }

    public static ConConf url(String database) {
      return new ConConf(database);
    }

    private ConConf(final String url) {
      if (url != null) {
        this.url = url;
      }
    }

    public String getUrl() {
      return url;
    }

    public String getUser() {
      return user;
    }

    public ConConf withUser(final String user) {
      this.user = user;
      return this;
    }

    public String getPassword() {
      return password;
    }

    public ConConf withPassword(final String password) {
      this.password = password;
      return this;
    }
  }
}
