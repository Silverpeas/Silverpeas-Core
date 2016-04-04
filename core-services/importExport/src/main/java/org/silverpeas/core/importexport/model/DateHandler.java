/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.importexport.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

/**
 * The FieldHandler for the Date class
 */
public class DateHandler extends GeneralizedFieldHandler {

  private static final String FORMAT = "yyyy-MM-dd";

  /**
   * Creates a new MyDateHandler instance
   */
  public DateHandler() {
    super();
  }

  /**
   * This method is used to convert the value when the getValue method is called. The getValue
   * method will obtain the actual field value from given 'parent' object. This convert method is
   * then invoked with the field's value. The value returned from this method will be the actual
   * value returned by getValue method.
   * @param value the object value to convert after performing a get operation
   * @return the converted value.
   */
  public Object convertUponGet(Object value) {
    if (value == null)
      return null;
    SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
    Date date = (Date) value;
    return formatter.format(date);
  }

  /**
   * This method is used to convert the value when the setValue method is called. The setValue
   * method will call this method to obtain the converted value. The converted value will then be
   * used as the value to set for the field.
   * @param value the object value to convert before performing a set operation
   * @return the converted value.
   */
  public Object convertUponSet(Object value) {
    SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
    Date date = null;
    try {
      date = formatter.parse((String) value);
    } catch (ParseException px) {
      throw new IllegalArgumentException(px.getMessage());
    }
    return date;
  }

  /**
   * Returns the class type for the field that this GeneralizedFieldHandler converts to and from.
   * This should be the type that is used in the object model.
   * @return the class type of of the field
   */
  public Class getFieldType() {
    return Date.class;
  }

  /**
   * Creates a new instance of the object described by this field.
   * @param parent The object for which the field is created
   * @return A new instance of the field's value
   * @throws IllegalStateException This field is a simple type and cannot be instantiated
   */
  public Object newInstance(Object parent) throws IllegalStateException {
    // -- Since it's marked as a string...just return null,
    // -- it's not needed.
    return null;
  }

}
