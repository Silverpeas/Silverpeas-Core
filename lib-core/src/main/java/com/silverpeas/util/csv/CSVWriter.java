/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.stratelia.webactiv.util.ResourceLocator;

public class CSVWriter {
  protected int m_nbCols = 0;
  protected String[] m_colNames;
  protected String[] m_colTypes;
  protected String[] m_colDefaultValues;
  protected String m_separator;
  protected ResourceLocator m_utilMessages;

  // properties spécifiques éventuellement en plus
  protected int m_specificNbCols = 0;
  protected String[] m_specificColNames;
  protected String[] m_specificColTypes;
  protected String[] m_specificParameterNames;

  /**
   * Constructeur
   */
  public CSVWriter(String language) {
    m_utilMessages = new ResourceLocator("com.silverpeas.util.multilang.util",
        language);
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName,
      String separator) {
    ResourceLocator rs = new ResourceLocator(propertiesFile, "");

    m_colNames = rs.getStringArray(rootPropertyName, ".Name", -1);
    m_nbCols = m_colNames.length;
    m_colTypes = rs.getStringArray(rootPropertyName, ".Type", m_nbCols);
    m_colDefaultValues = rs.getStringArray(rootPropertyName, ".Default",
        m_nbCols);
    m_separator = separator;
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName,
      String separator, String specificPropertiesFile,
      String specificRootPropertyName) {
    ResourceLocator rs = new ResourceLocator(propertiesFile, "");

    m_colNames = rs.getStringArray(rootPropertyName, ".Name", -1);
    m_nbCols = m_colNames.length;
    m_colTypes = rs.getStringArray(rootPropertyName, ".Type", m_nbCols);
    m_colDefaultValues = rs.getStringArray(rootPropertyName, ".Default",
        m_nbCols);
    m_separator = separator;

    ResourceLocator specificRs = new ResourceLocator(specificPropertiesFile, "");
    m_specificColNames = specificRs.getStringArray(specificRootPropertyName,
        ".Name", -1);
    m_specificNbCols = m_specificColNames.length;

    m_specificColTypes = specificRs.getStringArray(specificRootPropertyName,
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

}
