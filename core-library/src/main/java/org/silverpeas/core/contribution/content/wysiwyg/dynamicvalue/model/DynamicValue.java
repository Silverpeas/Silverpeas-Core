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

package org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.model;

import java.io.Serializable;
import java.util.Date;

/**
 * class used to store data information used by the dynamic value functionality
 */
public class DynamicValue implements Serializable {

  private static final long serialVersionUID = 1799321859007075096L;
  /**
   * key is used in HTML code as follows : "(%<key>%)". This key is visible only in HTML editor.
   */
  private String key = null;
  /**
   * value relating to a key this value replace the key when user read the content
   */
  private String value = null;
  /**
   * start date of key validity
   */
  private Date startDate = null;
  /**
   * end date of key validity
   */
  private Date enDate = null;

  /**
   * default constructor
   */
  public DynamicValue() {
  }

  /**
   * full constructor
   * @param key the key is used in HTML code as follows : "(%<key>%)".
   * @param value the value relating to a key this value replace the key when user read the content
   * @param startDate the start date of key validity
   * @param endDate the end date of key validity
   */
  public DynamicValue(String key, String value, Date startDate, Date endDate) {
    this.key = key;
    this.value = value;
    this.startDate = startDate;
    this.enDate = endDate;

  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * sets the key
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the enDate
   */
  public Date getEnDate() {
    return enDate;
  }

  /**
   * @param enDate the enDate to set
   */
  public void setEnDate(Date enDate) {
    this.enDate = enDate;
  }

}
