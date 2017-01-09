/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.model.Escapable;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.validate.ValidationException;

public class Html extends Property implements Escapable {

  public static final String X_ALT_DESC = "X-ALT-DESC";

  private static final long serialVersionUID = 7287564228220558361L;
  private static final String HTML = X_ALT_DESC + ";FMTTYPE=text/html";

  private String value;

  /**
   * Default constructor.
   */
  public Html() {
    super(HTML, PropertyFactoryImpl.getInstance());
  }

  /**
   * @param aValue a value string for this component
   */
  public Html(final String aValue) {
    super(HTML, PropertyFactoryImpl.getInstance());
    setValue(aValue);
  }

  /**
   * @param aList a list of parameters for this component
   * @param aValue a value string for this component
   */
  public Html(final ParameterList aList, final String aValue) {
    super(HTML, aList, PropertyFactoryImpl.getInstance());
    setValue(aValue);
  }

  /**
   * {@inheritDoc}
   */
  public final void validate() throws ValidationException {
  }

  /**
   * {@inheritDoc}
   */
  public final String getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   */
  public final void setValue(final String aValue) {
    this.value = aValue;
  }
}
