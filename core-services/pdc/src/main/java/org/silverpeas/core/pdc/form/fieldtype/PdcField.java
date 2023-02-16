/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.form.fieldtype;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.contribution.content.form.AbstractField;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.pdc.form.displayers.PdcFieldDisplayer;

/**
 * A PDC field describes the positions of a contribution on the axis of the PdC (Plan of
 * Classification)
 * @author ahedin
 * @see PdcFieldDisplayer
 */
public class PdcField extends AbstractField {

  private static final long serialVersionUID = 1L;

  public static final String TYPE = "pdc";

  private String value = "";

  public PdcField() {
    // nothing  to do
  }

  public String getTypeName() {
    return TYPE;
  }

  public boolean acceptObjectValue(Object value) {
    return !isReadOnly();
  }

  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  public boolean acceptValue(String value) {
    return !isReadOnly();
  }

  public boolean acceptValue(String value, String lang) {
    return !isReadOnly();
  }

  public boolean isReadOnly() {
    return false;
  }

  public Object getObjectValue() {
    return getStringValue();
  }

  public String getStringValue() {
    return value;
  }

  public String getValue() {
    return getStringValue();
  }

  public String getValue(String lang) {
    return getStringValue();
  }

  public boolean isNull() {
    return (getStringValue() == null || getStringValue().trim().equals(""));
  }

  public void setNull() {
    setStringValue(null);
  }

  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setStringValue((String) value);
    } else {
      if (value != null) {
        throw new FormException("PdcField.setObjectValue", "form.EXP_NOT_A_STRING");
      } else {
        setNull();
      }
    }
  }

  public void setStringValue(String value) {
    this.value = value;
  }

  public void setValue(String value) throws FormException {
    setStringValue(value);
  }

  public void setValue(String value, String lang) throws FormException {
    setStringValue(value);
  }

  @Override
  public int compareTo(Field o) {
    String s = getStringValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof PdcField) {
      String t = o.getStringValue();
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else if (o != null) {
      String t = o.getValue("");
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PdcField)) {
      return false;
    }

    final PdcField pdcField = (PdcField) o;
    return compareTo(pdcField) == 0;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getValue()).toHashCode();
  }
}