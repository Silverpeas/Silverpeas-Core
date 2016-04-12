/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util.csv;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.exception.UtilTrappedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CSVReader {
  protected int nbCols = 0;
  protected List<String> colNames;
  protected List<String> colTypes;
  protected List<String> colDefaultValues;
  protected List<String> colMandatory;

  protected String separator;
  protected LocalizationBundle utilMessages;

  // properties specifiques eventuellement en plus
  protected int specificNbCols = 0;
  protected List<String> specificColNames;
  protected List<Integer> specificColMaxLengths;
  protected List<String> specificColTypes;
  protected List<String> specificColMandatory;
  protected List<String> specificParameterNames;

  // Active control file columns/object columns
  private boolean columnNumberControlEnabled = true;
  // Active control file extra columns/object columns
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
   * @param language the language of the UI
   */
  public CSVReader(String language) {
    utilMessages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName, String separator) {
    SettingBundle p = ResourceLocator.getSettingBundle(propertiesFile);

    colNames = p.getStringList(rootPropertyName, "Name");
    nbCols = colNames.size();

    colTypes = p.getStringList(rootPropertyName, "Type", nbCols);
    colDefaultValues = p.getStringList(rootPropertyName, "Default", nbCols);
    colMandatory = p.getStringList(rootPropertyName, "Mandatory", nbCols);
    this.separator = separator;
  }

  public void initCSVFormat(String propertiesFile, String rootPropertyName, String separator,
      String specificPropertiesFile, String specificRootPropertyName) {
    initCSVFormat(propertiesFile, rootPropertyName, separator);

    SettingBundle sP = ResourceLocator.getSettingBundle(specificPropertiesFile);
    specificColNames = sP.getStringList(specificRootPropertyName, "Name", -1);
    specificNbCols = specificColNames.size();

    specificColTypes = sP.getStringList(specificRootPropertyName, "Type", specificNbCols);
    specificColMaxLengths = sP.getStringList(specificRootPropertyName, "MaxLength", specificNbCols)
        .asIntegerList(DomainProperty.DEFAULT_MAX_LENGTH);
    for (int i = 0; i < specificNbCols; i++) {
      // Adjusting the type if necessary (so, set default one if necessary)
      final String specificColType = specificColTypes.get(i);
      if (!Variant.TYPE_STRING.equals(specificColType) &&
          !Variant.TYPE_INT.equals(specificColType) &&
          !Variant.TYPE_BOOLEAN.equals(specificColType) &&
          !Variant.TYPE_FLOAT.equals(specificColType) &&
          !Variant.TYPE_DATEFR.equals(specificColType) &&
          !Variant.TYPE_DATEUS.equals(specificColType) &&
          !Variant.TYPE_STRING_ARRAY.equals(specificColType) &&
          !Variant.TYPE_LONG.equals(specificColType)) {

        specificColTypes.set(i, Variant.TYPE_STRING);
      }
    }

    specificColMandatory = sP.getStringList(specificRootPropertyName, "Mandatory", specificNbCols);

    specificParameterNames = specificColNames;
  }

  public Variant[][] parseStream(InputStream is) throws UtilTrappedException {
    List<Variant[]> finalResult = new ArrayList<>();
    int lineNumber = 1;
    StringBuilder listErrors = new StringBuilder("");
    try {
      BufferedReader rb = new BufferedReader(new InputStreamReader(is));
      String theLine = rb.readLine();
      if (theLine != null && !isExtraColumnsControlEnabled()) {
        StringTokenizer st = new StringTokenizer(theLine, separator);
        setSpecificNbCols(st.countTokens() - nbCols);
      }
      while (theLine != null) {

        if (theLine.trim().length() > 0) {
          try {
            finalResult.add(parseLine(theLine, lineNumber));
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
      return finalResult.toArray(new Variant[0][0]);
    } catch (IOException e) {
      throw new UtilTrappedException("CSVReader.parseStream", SilverpeasException.ERROR,
          "util.EX_TRANSMITING_CSV",
          utilMessages.getString("util.ligne") + " = " + Integer.toString(lineNumber) + "\n" +
              listErrors.toString(), e);
    }
  }

  public Variant[] parseLine(String theLine, int lineNumber) throws UtilTrappedException {
    int nbColsTotal = nbCols + specificNbCols;
    Variant[] finalResult = new Variant[nbColsTotal];
    int i, j;
    StringBuilder listErrors = new StringBuilder("");

    int start = 0;
    int end = theLine.indexOf(separator, start);
    String theValue;
    for (i = 0; i < nbCols; i++) {
      if (end == -1) {
        theValue = theLine.substring(start).trim();
      } else {
        theValue = theLine.substring(start, end).trim();
      }
      final String colType = colTypes.get(i);
      try {
        if (theValue.length() <= 0) {
          if (Boolean.parseBoolean(colMandatory.get(i))) {
            listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
                .append(Integer.toString(lineNumber)).append(", ");
            listErrors.append(utilMessages.getString("util.colonne")).append(" = ")
                .append(Integer.toString(i + 1)).append(", ");
            listErrors.append(utilMessages.getString("util.errorMandatory"))
                .append(utilMessages.getString("util.valeur")).append(" = ").append(theValue)
                .append(", ");
            listErrors.append(utilMessages.getString("util.type")).append(" = ").append(colType)
                .append("<br/>");
          } else {
            theValue = colDefaultValues.get(i);
          }
        }
        if (Variant.isArrayType(colType)) {
          finalResult[i] = new Variant(parseArrayValue(theValue), colType);
        } else {
          finalResult[i] = new Variant(theValue, colType);
        }

      } catch (UtilException e) {
        listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
            .append(Integer.toString(lineNumber)).append(", ");
        listErrors.append(utilMessages.getString("util.colonne")).append(" = ")
            .append(Integer.toString(i + 1)).append(", ");
        listErrors.append(utilMessages.getString("util.errorType"))
            .append(utilMessages.getString("util.valeur")).append(" = ").append(theValue)
            .append(", ");
        listErrors.append(utilMessages.getString("util.type")).append(" = ").append(colType)
            .append("<br/>");
      }
      start = end + 1;
      if (start == 0) {
        start = -1;
      }

      if ((i < nbCols - 1) && (Variant.isArrayType(colTypes.get(i + 1)))) {
        // End the parse putting the rest of the line into an array
        end = -1;
      } else {
        end = theLine.indexOf(separator, start);
      }
      if (isColumnNumberControlEnabled() && (i < nbCols - 2) && (end == -1)) {
        // Not enough columns
        listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
            .append(Integer.toString(lineNumber)).append(", ");
        listErrors.append(Integer.toString(i + 2)).append(" ")
            .append(utilMessages.getString("util.colonnesAttendues")).append(" ")
            .append(Integer.toString(nbCols)).append(" ")
            .append(utilMessages.getString("util.attendues")).append("<br/>");
      }
    }

    // Processing specific data
    j = nbCols;
    for (i = 0; i < specificNbCols; i++) {
      if (start == -1) {
        theValue = ""; // empty value as default
        end = -2;
      } else if (end == -1) {
        theValue = theLine.substring(start).trim();
      } else {
        theValue = theLine.substring(start, end).trim();
      }
      if (isExtraColumnsControlEnabled()) {
        final String specificColType = specificColTypes.get(i);
        try {
          finalResult[j] = new Variant(theValue, specificColType);

          if (theValue.length() <= 0) {
            final String specificColMandatory = this.specificColMandatory.get(i);
            if (Boolean.parseBoolean(specificColMandatory)) {
              listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
                  .append(Integer.toString(lineNumber)).append(", ");
              listErrors.append(utilMessages.getString("util.colonne")).append(" = ")
                  .append(Integer.toString(i + 1)).append(", ");
              listErrors.append(utilMessages.getString("util.errorMandatory"))
                  .append(utilMessages.getString("util.valeur")).append(" = ").append(theValue)
                  .append(", ");
              listErrors.append(utilMessages.getString("util.type")).append(" = ")
                  .append(specificColType).append("<br/>");
            }
          }
        } catch (UtilException e) {
          listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
              .append(Integer.toString(lineNumber)).append(", ");
          listErrors.append(utilMessages.getString("util.colonne")).append(" = ")
              .append(Integer.toString(j + 1)).append(", ");
          listErrors.append(utilMessages.getString("util.errorType"))
              .append(utilMessages.getString("util.valeur")).append(" = ").append(theValue)
              .append(", ");
          listErrors.append(utilMessages.getString("util.type")).append(" = ")
              .append(specificColType).append("<br/>");
        }
      } else {
        try {
          finalResult[j] = new Variant(theValue, "STRING");
        } catch (UtilException ignored) {
        }
      }
      start = end + 1;
      if (start == 0) {
        start = -1;
      }
      if (isExtraColumnsControlEnabled() && (i < specificNbCols - 1) &&
          (Variant.isArrayType(specificColTypes.get(i + 1)))) {
        // End the parse putting the rest of the line into an array
        end = -1;
      } else {
        end = theLine.indexOf(separator, start);
      }
      if (isColumnNumberControlEnabled() && (i < specificNbCols - 2) && (end == -1)) {
        // Not enough columns
        listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
            .append(Integer.toString(lineNumber)).append(", ");
        listErrors.append(Integer.toString(i + 2 + nbCols)).append(" ")
            .append(utilMessages.getString("util.colonnesAttendues")).append(" ")
            .append(Integer.toString(nbColsTotal)).append(" ")
            .append(utilMessages.getString("util.attendues")).append("<br/>");
      }

      j++;
    }

    // counting the number of not necessary column
    int nbColumns = nbColsTotal;
    while (start > -1) {
      nbColumns++;
      start = end + 1;
      if (start == 0) {
        start = -1;
      }
      end = theLine.indexOf(separator, start);
    }

    if (isColumnNumberControlEnabled() && nbColumns > nbColsTotal) {
      listErrors.append(utilMessages.getString("util.ligne")).append(" = ")
          .append(Integer.toString(lineNumber)).append(", ");
      listErrors.append(nbColumns).append(" ")
          .append(utilMessages.getString("util.colonnesAttendues")).append(" ")
          .append(Integer.toString(nbColsTotal)).append(" ")
          .append(utilMessages.getString("util.attendues")).append("<br/>");
    }

    if (listErrors.length() > 0) {
      throw new UtilTrappedException("CSVReader.parseLine", SilverpeasException.ERROR,
          "util.EX_PARSING_CSV_VALUE", listErrors.toString());
    }
    return finalResult;
  }

  protected String[] parseArrayValue(String arrayValue) {
    int start, end;
    String theValue;
    ArrayList<String> ar = new ArrayList<>();
    boolean haveToContinue = true;

    start = 0;
    while (haveToContinue) {
      end = arrayValue.indexOf(separator, start);

      if (end == -1) {
        theValue = arrayValue.substring(start).trim();
        haveToContinue = false;
      } else {
        theValue = arrayValue.substring(start, end).trim();
      }
      if ((theValue.length() <= 0)) {
        theValue = "";
      }

      ar.add(theValue);

      start = end + 1;
    }
    return ar.toArray(new String[ar.size()]);
  }

  /**
   * @return the number of standard column.
   */
  public int getNbCols() {
    return nbCols;
  }

  /**
   * @return the number of specific columns.
   */
  public int getSpecificNbCols() {
    return specificNbCols;
  }

  /**
   * @param specificNbCols the number of specific columns to set.
   */
  public void setSpecificNbCols(int specificNbCols) {
    this.specificNbCols = specificNbCols;
  }

  /**
   * @return the type of specific column i.
   */
  public String getSpecificColType(int i) {
    return specificColTypes.get(i);
  }

  /**
   * @return the parameter name of specific column i.
   */
  public String getSpecificParameterName(int i) {
    return specificParameterNames.get(i);
  }

  /**
   * @return the maximum length of specific column i.
   */
  public int getSpecificColMaxLength(int i) {
    return specificColMaxLengths.get(i);
  }
}
