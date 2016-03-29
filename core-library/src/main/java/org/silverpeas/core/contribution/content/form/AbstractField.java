/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form;

public abstract class AbstractField implements Field {

  private static final long serialVersionUID = 8716397233101185565L;
  private String name;
  private int occurrence;

  @Override
  public abstract int compareTo(Object o);

  @Override
  public abstract String getTypeName();

  @Override
  public abstract String getValue();

  @Override
  public abstract void setValue(String value) throws FormException;

  @Override
  public abstract boolean acceptValue(String value);

  @Override
  public abstract String getValue(String lang);

  @Override
  public abstract void setValue(String value, String lang) throws FormException;

  @Override
  public abstract boolean acceptValue(String value, String lang);

  @Override
  public abstract String getStringValue();

  @Override
  public abstract void setStringValue(String value) throws FormException;

  @Override
  public abstract boolean acceptStringValue(String value);

  @Override
  public abstract Object getObjectValue();

  @Override
  public abstract void setObjectValue(Object value) throws FormException;

  @Override
  public abstract boolean acceptObjectValue(Object value);

  @Override
  public abstract boolean isNull();

  @Override
  public abstract void setNull() throws FormException;

  @Override
  public int getOccurrence() {
    return occurrence;
  }

  public void setOccurrence(int i) {
    occurrence = i;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}