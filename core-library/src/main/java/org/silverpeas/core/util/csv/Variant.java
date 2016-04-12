/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/
package org.silverpeas.core.util.csv;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import java.text.ParseException;
import java.util.Date;

public final class Variant {

  public static final String TYPE_STRING = "STRING";
  public static final String TYPE_INT = "INT";
  public static final String TYPE_BOOLEAN = "BOOLEAN";
  public static final String TYPE_FLOAT = "FLOAT";
  public static final String TYPE_DATEFR = "DATEFR";
  public static final String TYPE_DATEUS = "DATEUS";
  // Array types must end the line
  public static final String TYPE_STRING_ARRAY = "SARRAY";
  public static final String TYPE_LONG = "LONG";
  protected String m_type;
  protected String[] m_asValue = new String[0];
  protected String m_sValue = "";
  protected int m_iValue = 0;
  protected boolean m_bValue = false;
  protected float m_fValue = 0;
  protected Date m_dValue = new Date();
  protected long m_lValue = 0;

  static public boolean isArrayType(String type) {
    boolean valret = false;
    if (TYPE_STRING_ARRAY.equals(type)) {
      valret = true;
    }
    return valret;
  }

  public Variant(String value, String type) throws UtilException {
    m_type = type;
    m_sValue = value;
    if (TYPE_INT.equals(m_type)) {
      m_sValue = value.trim();
      setValueInteger(value);
    }
    if (TYPE_LONG.equals(m_type)) {
      m_sValue = value.trim();
      setValueLong(value);
    }
    if (TYPE_BOOLEAN.equals(m_type)) {
      m_sValue = value.trim();
      setValueBoolean(value);
    }
    if (TYPE_FLOAT.equals(m_type)) {
      // Eventualy replace the char coma by the char point
      m_sValue = value.trim().replace(',', '.');
      setValueFloat(value);
    }
    if ((TYPE_DATEFR.equals(m_type)) || (TYPE_DATEUS.equals(m_type))) {
      m_sValue = value.trim();
      setValueDate(value);
    }
  }

  public Variant(String[] values, String type) {
    m_type = type;
    if (TYPE_STRING_ARRAY.equals(m_type)) {
      setValueStringArray(values);
    }
  }

  public String getDefaultType() {
    return m_type;
  }

  public String[] getValueStringArray() {
    return m_asValue;
  }

  public String getValueString() {
    return m_sValue;
  }

  public int getValueInteger() {
    return m_iValue;
  }

  public long getValueLong() {
    return m_lValue;
  }

  public boolean getValueBoolean() {
    return m_bValue;
  }

  public float getValueFloat() {
    return m_fValue;
  }

  public Date getValueDate() {
    return m_dValue;
  }

  protected String[] setValueStringArray(String[] asValue) {
    if (asValue != null) {
      m_asValue = asValue.clone();
    } else {
      m_asValue = new String[0];
    }
    return m_asValue;
  }

  protected int setValueInteger(String value) throws UtilException {
    try {
      m_iValue = Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      throw new UtilException("Variant.setValueInteger",
          SilverpeasException.ERROR, "root.EX_INVALID_ARG", "Value=" + value
          + " Type=" + m_type, e);
    }
    return m_iValue;
  }

  protected long setValueLong(String value) throws UtilException {
    try {
      m_lValue = Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      throw new UtilException("Variant.setValueLong",
          SilverpeasException.ERROR, "root.EX_INVALID_ARG", "Value=" + value
          + " Type=" + m_type, e);
    }
    return m_lValue;
  }

  protected boolean setValueBoolean(String value) throws UtilException {
    boolean error = false;

    if (value != null) {
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
          || value.equalsIgnoreCase("oui") || value.equalsIgnoreCase("1")) {
        m_bValue = true;
      } else if (value.equalsIgnoreCase("false")
          || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("non")
          || value.equalsIgnoreCase("0")) {
        m_bValue = false;
      } else {
        error = true;
      }
    } else {
      error = true;
    }
    if (error) {
      throw new UtilException("Variant.setValueBoolean",
          SilverpeasException.ERROR, "root.EX_INVALID_ARG", "Value=" + value
          + " Type=" + m_type);
    }
    return m_bValue;
  }

  protected float setValueFloat(String value) throws UtilException {
    try {
      m_fValue = Float.parseFloat(value.trim().replace(',', '.'));
    } catch (NumberFormatException e) {
      throw new UtilException("Variant.setValueFloat",
          SilverpeasException.ERROR, "root.EX_INVALID_ARG", "Value=" + value
          + " Type=" + m_type, e);
    }
    return m_fValue;
  }

  protected Date setValueDate(String value) throws UtilException {
    try {
      if (TYPE_DATEUS.equals(m_type)) {
        m_dValue = DateUtil.stringToDate(value, "en");
      } else {
        m_dValue = DateUtil.stringToDate(value, "fr");
      }
    } catch (ParseException e) {
      throw new UtilException("Variant.setValueDate",
          SilverpeasException.ERROR, "root.EX_INVALID_ARG", "Value=" + value
          + " Type=" + m_type, e);
    }
    return m_dValue;
  }
}
