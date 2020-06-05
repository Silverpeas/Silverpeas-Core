/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import net.fortuna.ical4j.model.PropertyFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class HtmlProperty extends Property implements Escapable {

  public static final String X_ALT_DESC = "X-ALT-DESC";
  public static final String PROPERTY_NAME = X_ALT_DESC + ";FMTTYPE=text/html";
  public static final HtmlPropertyFactory FACTORY = new HtmlPropertyFactory();

  private static final long serialVersionUID = 7287564228220558361L;
  private String value;

  /**
   * Default constructor.
   */
  public HtmlProperty() {
    super(PROPERTY_NAME, FACTORY);
  }

  /**
   * @param aValue a value string for this component
   */
  public HtmlProperty(final String aValue) {
    super(PROPERTY_NAME, FACTORY);
    setValue(aValue);
  }

  /**
   * @param aList a list of parameters for this component
   * @param aValue a value string for this component
   */
  public HtmlProperty(final ParameterList aList, final String aValue) {
    super(PROPERTY_NAME, aList, FACTORY);
    setValue(aValue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void validate() {
    // no validation here
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setValue(final String aValue) {
    this.value = aValue;
  }

  public static class HtmlPropertyFactory implements PropertyFactory<Property> {

    public HtmlPropertyFactory() {
      super();
    }

    @Override
    public Property createProperty() {
      return new HtmlProperty();
    }

    @Override
    public Property createProperty(final ParameterList parameters, final String value)
        throws IOException, URISyntaxException, ParseException {
      return new HtmlProperty(parameters, value);
    }

    @Override
    public boolean supports(final String name) {
      return PROPERTY_NAME.equals(name);
    }
  }
}
