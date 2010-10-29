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

package com.silverpeas.util.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;
import java.util.List;

public class CSVReader extends SilverpeasSettings {
  protected int m_nbCols = 0;
  protected String[] m_colNames;
  protected String[] m_colTypes;
  protected String[] m_colDefaultValues;
  protected String[] m_colMandatory;
  
  protected String m_separator;
  protected ResourceLocator m_utilMessages;

  // properties specifiques eventuellement en plus
  protected int m_specificNbCols = 0;
  protected String[] m_specificColNames;
  protected String[] m_specificColTypes;
  protected String[] m_specificParameterNames;
  
  //Active control file columns/object columns
  private boolean columnNumberControlEnabled = true;
  //Active control file extra columns/object columns
  private boolean extraColumnsControlEnabled = true;

  public boolean isExtraColumnsControlEnabled() {
    return extraColumnsControlEnabled;
  }

  public void setExtraColumnsControlEnabled(boolean extraColumnsControlEnabled) {
    this.extraColumnsControlEnabled = extraColumnsControlEnabled;
  }

  public boolean isColumnNumberControlEnabled() {
    return columnNumberControlEnabled;
  }

  public void setColumnNumberControlEnabled(boolean columnNumberControlEnabled) {
    this.columnNumberControlEnabled = columnNumberControlEnabled;
  }

  /**
   * Constructeur
   * @param language 
   */
  public CSVReader(String language) {
    m_utilMessages = new ResourceLocator("com.silverpeas.util.multilang.util", language);
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName,
      String separator) {
    ResourceLocator rs = new ResourceLocator(propertiesFile, "");

    m_colNames = readStringArray(rs, rootPropertyName, ".Name", -1);
    m_nbCols = m_colNames.length;
    m_colTypes = readStringArray(rs, rootPropertyName, ".Type", m_nbCols);
    m_colDefaultValues = readStringArray(rs, rootPropertyName, ".Default",
        m_nbCols);
    m_colMandatory = readStringArray(rs, rootPropertyName, ".Mandatory",
        m_nbCols);
    m_separator = separator;
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName,
      String separator, String specificPropertiesFile,
      String specificRootPropertyName) {
    ResourceLocator rs = new ResourceLocator(propertiesFile, "");

    m_colNames = readStringArray(rs, rootPropertyName, ".Name", -1);
    m_nbCols = m_colNames.length;
    m_colTypes = readStringArray(rs, rootPropertyName, ".Type", m_nbCols);
    m_colDefaultValues = readStringArray(rs, rootPropertyName, ".Default",
        m_nbCols);
    m_colMandatory = readStringArray(rs, rootPropertyName, ".Mandatory",
        m_nbCols);
    m_separator = separator;

    ResourceLocator specificRs = new ResourceLocator(specificPropertiesFile, "");
    m_specificColNames = readStringArray(specificRs, specificRootPropertyName,
        ".Name", -1);
    m_specificNbCols = m_specificColNames.length;

    m_specificColTypes = readStringArray(specificRs, specificRootPropertyName,
        ".Type", m_specificNbCols);
    for (int i = 0; i < m_specificNbCols; i++) {
      if (!Variant.TYPE_STRING.equals(m_specificColTypes[i])
          && !Variant.TYPE_INT.equals(m_specificColTypes[i])
          && !Variant.TYPE_BOOLEAN.equals(m_specificColTypes[i])
          && !Variant.TYPE_FLOAT.equals(m_specificColTypes[i])
          && !Variant.TYPE_DATEFR.equals(m_specificColTypes[i])
          && !Variant.TYPE_DATEUS.equals(m_specificColTypes[i])
          && !Variant.TYPE_STRING_ARRAY.equals(m_specificColTypes[i])
          && !Variant.TYPE_LONG.equals(m_specificColTypes[i])) {

        m_specificColTypes[i] = Variant.TYPE_STRING;
      }
    }
    m_specificParameterNames = m_specificColNames;
  }

  public Variant[][] parseStream(InputStream is) throws UtilTrappedException {
    List<Variant[]> valret = new ArrayList<Variant[]>();
    int lineNumber = 1;
    StringBuilder listErrors = new StringBuilder("");
    try {
      BufferedReader rb = new BufferedReader(new InputStreamReader(is));
      String theLine = rb.readLine();
      if (theLine != null && !isExtraColumnsControlEnabled()) {
        StringTokenizer st = new StringTokenizer(theLine, m_separator);
        setM_specificNbCols(st.countTokens()-m_nbCols);
      }        
      while (theLine != null) {
        SilverTrace.info("util", "CSVReader.parseStream()",
            "root.MSG_PARAM_VALUE", "Line=" + lineNumber);
        if (theLine.trim().length() > 0) {
          try {
            valret.add(parseLine(theLine, lineNumber));
          } catch (UtilTrappedException u) {
            listErrors.append(u.getExtraInfos()).append('\n');
          }
        }
        lineNumber++;
        theLine = rb.readLine();
      }
      if (listErrors.length() > 0) {
        throw new UtilTrappedException("CSVReader.parseStream", SilverpeasException.ERROR, 
            "util.EX_PARSING_CSV_VALUE", listErrors.toString());
      }
      return valret.toArray(new Variant[0][0]);
    } catch (IOException e) {
      throw new UtilTrappedException("CSVReader.parseStream", SilverpeasException.ERROR, 
          "util.EX_TRANSMITING_CSV", m_utilMessages.getString("util.ligne") + " = "
          + Integer.toString(lineNumber) + "\n" + listErrors.toString(), e);
    }
  }

  public Variant[] parseLine(String theLine, int lineNumber)
      throws UtilTrappedException {
    int nbColsTotal = m_nbCols + m_specificNbCols;
    Variant[] valret = new Variant[nbColsTotal];
    int i, j;
    StringBuilder listErrors = new StringBuilder("");

    int start = 0;
    int end = theLine.indexOf(m_separator, start);
    String theValue;
    for (i = 0; i < m_nbCols; i++) {
      if (end == -1) {
        theValue = theLine.substring(start).trim();
      } else {
        theValue = theLine.substring(start, end).trim();
      }
      try {
        if ((theValue == null) || (theValue.length() <= 0)) {
          if (Boolean.parseBoolean(m_colMandatory[i]))
          {
            listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
                Integer.toString(lineNumber)).append(", ");
            listErrors.append(m_utilMessages.getString("util.colonne")).append(" = ").append(
                Integer.toString(i + 1)).append(", ");
            listErrors.append(m_utilMessages.getString("util.errorMandatory")).append(
                m_utilMessages.getString("util.valeur")).append(" = ").append(theValue).append(", ");
            listErrors.append(m_utilMessages.getString("util.type")).append(" = ").append(
                m_colTypes[i]).append("<br/>");
          }
          else {
            theValue = m_colDefaultValues[i];
          }
        }
        if (Variant.isArrayType(m_colTypes[i])) {
          valret[i] = new Variant(parseArrayValue(theValue), m_colTypes[i]);
        } else {
          valret[i] = new Variant(theValue, m_colTypes[i]);
        }
        SilverTrace.info("util", "CSVReader.parseLine()",
            "root.MSG_PARAM_VALUE", "Token=" + theValue);
      } catch (UtilException e) {
        listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
            Integer.toString(lineNumber)).append(", ");
        listErrors.append(m_utilMessages.getString("util.colonne")).append(" = ").append(
            Integer.toString(i + 1)).append(", ");
        listErrors.append(m_utilMessages.getString("util.errorType")).append(
            m_utilMessages.getString("util.valeur")).append(" = ").append(theValue).append(", ");
        listErrors.append(m_utilMessages.getString("util.type")).append(" = ").append(
            m_colTypes[i]).append("<br/>");
      }
      start = end + 1;
      if (start == 0) {
        start = -1;
      }

      if ((i < m_nbCols - 1) && (Variant.isArrayType(m_colTypes[i + 1]))) { // End
        // the
        // parse
        // putting
        // the
        // rest
        // of
        // the
        // line
        // into
        // an
        // array
        end = -1;
      } else {
        end = theLine.indexOf(m_separator, start);
      }
      if (isColumnNumberControlEnabled() && (i < m_nbCols - 2) && (end == -1)) { // Not enough columns
        listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
            Integer.toString(lineNumber)).append(", ");
        listErrors.append(Integer.toString(i + 2)).append(" ").append(
            m_utilMessages.getString("util.colonnesAttendues")).append(" ").append(
            Integer.toString(m_nbCols)).append(" ").append(m_utilMessages.getString(
            "util.attendues")).append("<br/>");
      }
    }

    // traitement des donnees specifiques
    j = m_nbCols;
    for (i = 0; i < m_specificNbCols; i++) {
      if (start == -1) {
        theValue = ""; // remplace la donnee specifique manquante par une valeur
        // vide
        end = -2;
      } else if (end == -1) {
        theValue = theLine.substring(start).trim();
      } else {
        theValue = theLine.substring(start, end).trim();
      }
      if (isExtraColumnsControlEnabled())
      {
        try {
          valret[j] = new Variant(theValue, m_specificColTypes[i]);
          SilverTrace.info("util", "CSVReader.parseLine()",
              "root.MSG_PARAM_VALUE", "Token=" + theValue);
          if ((theValue == null) || (theValue.length() <= 0)) {
            if (Boolean.parseBoolean(m_colMandatory[i]))
            {
              listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
                  Integer.toString(lineNumber)).append(", ");
              listErrors.append(m_utilMessages.getString("util.colonne")).append(" = ").append(
                  Integer.toString(i + 1)).append(", ");
              listErrors.append(m_utilMessages.getString("util.errorMandatory")).append(
                  m_utilMessages.getString("util.valeur")).append(" = ").append(theValue).append(
                  ", ");
              listErrors.append(m_utilMessages.getString("util.type")).append(" = ").append(
                  m_colTypes[i]).append("<br/>");
            }
          }
        } catch (UtilException e) {
          listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
              Integer.toString(lineNumber)).append(", ");
          listErrors.append(m_utilMessages.getString("util.colonne")).append(" = ").append(
              Integer.toString(j + 1)).append(", ");
          listErrors.append(m_utilMessages.getString("util.errorType")).append(
              m_utilMessages.getString("util.valeur")).append(" = ").append(theValue).append(", ");
          listErrors.append(m_utilMessages.getString("util.type")).append(" = ").append(
              m_specificColTypes[i]).append("<br/>");
        }
      }
      else {
        try {
          valret[j] = new Variant(theValue, "STRING");
        } catch (UtilException e) {
      SilverTrace.info("util", "CSVReader.parseLine()",
          "root.MSG_PARAM_VALUE", "Token=" + theValue);
        }
      }
      start = end + 1;
      if (start == 0) {
        start = -1;
      }
      if (isExtraColumnsControlEnabled() && (i < m_specificNbCols - 1)
          && (Variant.isArrayType(m_specificColTypes[i + 1]))) { // End the
        // parse
        // putting the
        // rest of the
        // line into an
        // array
        end = -1;
      } else {
        end = theLine.indexOf(m_separator, start);
      }
      if (isColumnNumberControlEnabled() && (i < m_specificNbCols - 2) && (end == -1)) { // Not enough columns
        listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
            Integer.toString(lineNumber)).append(", ");
        listErrors.append(Integer.toString(i + 2 + m_nbCols)).append(" ").append(
            m_utilMessages.getString("util.colonnesAttendues")).append(" ").append(
            Integer.toString(nbColsTotal)).append(" ").append(
            m_utilMessages.getString("util.attendues")).append("<br/>");
      }

      j++;
    }

    // compte le nbre de colonnes en trop
    int nbColonnes = nbColsTotal;
    while (start > -1) {
      nbColonnes++;
      start = end + 1;
      if (start == 0) {
        start = -1;
      }
      end = theLine.indexOf(m_separator, start);
    }

    if (isColumnNumberControlEnabled() && nbColonnes > nbColsTotal) {
      listErrors.append(m_utilMessages.getString("util.ligne")).append(" = ").append(
          Integer.toString(lineNumber)).append(", ");
      listErrors.append(nbColonnes).append(" ").append(
          m_utilMessages.getString("util.colonnesAttendues")).append(" ").append(
          Integer.toString(nbColsTotal)).append(" ").append(
          m_utilMessages.getString("util.attendues")).append("<br/>");
    }

    if (listErrors.length() > 0) {
      throw new UtilTrappedException("CSVReader.parseLine",
          SilverpeasException.ERROR, "util.EX_PARSING_CSV_VALUE", listErrors
          .toString());
    }
    return valret;
  }

  protected String[] parseArrayValue(String arrayValue) {
    int start, end;
    String theValue;
    ArrayList<String> ar = new ArrayList<String>();
    boolean haveToContinue = true;

    start = 0;
    while (haveToContinue) {
      end = arrayValue.indexOf(m_separator, start);

      if (end == -1) {
        theValue = arrayValue.substring(start).trim();
        haveToContinue = false;
      } else {
        theValue = arrayValue.substring(start, end).trim();
      }
      if ((theValue == null) || (theValue.length() <= 0)) {
        theValue = "";
      }

      ar.add(theValue);

      start = end + 1;
    }
    return ar.toArray(new String[0]);
  }

  /**
   * @return Returns the m_nbCols.
   */
  public int getM_nbCols() {
    return m_nbCols;
  }

  /**
   * @return Returns the m_separator.
   */
  public String getM_separator() {
    return m_separator;
  }

  /**
   * @return Returns the m_colDefaultValues.
   */
  public String[] getM_colDefaultValues() {
    return m_colDefaultValues;
  }

  public String[] getM_colMandatory() {
    return m_colMandatory;
  }

  /**
   * @return Returns the m_colDefaultValues[i]
   */
  public String getM_colDefaultValues(int i) {
    return m_colDefaultValues[i];
  }

  /**
   * @return Returns the m_colTypes.
   */
  public String[] getM_colTypes() {
    return m_colTypes;
  }

  /**
   * @return Returns the m_colTypes[i]
   */
  public String getM_colTypes(int i) {
    return m_colTypes[i];
  }

  /**
   * @return Returns the m_specificNbCols.
   */
  public int getM_specificNbCols() {
    return m_specificNbCols;
  }

  /**
   * @param cols The m_specificNbCols to set.
   */
  public void setM_specificNbCols(int cols) {
    m_specificNbCols = cols;
  }

  /**
   * @return Returns the m_specificColNames.
   */
  public String[] getM_specificColNames() {
    return m_specificColNames;
  }

  /**
   * @return Returns the m_specificColName index i.
   */
  public String getM_specificColName(int i) {
    return m_specificColNames[i];
  }

  /**
   * @return Returns the m_specificColTypes.
   */
  public String[] getM_specificColTypes() {
    return m_specificColTypes;
  }

  /**
   * @return Returns the m_specificColName index i.
   */
  public String getM_specificColType(int i) {
    return m_specificColTypes[i];
  }

  /**
   * @return Returns the m_specificParameterNames.
   */
  public String[] getM_specificParameterNames() {
    return m_specificParameterNames;
  }

  /**
   * @return Returns the m_specificParameterNames.
   */
  public String getM_specificParameterName(int i) {
    return m_specificParameterNames[i];
  }
}
