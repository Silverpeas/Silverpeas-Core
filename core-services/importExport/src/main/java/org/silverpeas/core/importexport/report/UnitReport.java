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

/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.silverpeas.core.importexport.report;

/**
 * @author tleroi
 */
public class UnitReport {

  // Variables
  private String itemName;
  private String label = null;
  private int status = -1;
  private int error = ERROR_NO_ERROR;
  // Constantes
  public final static int STATUS_PUBLICATION_CREATED = 0;
  public final static int STATUS_PUBLICATION_UPDATED = 1;
  public final static int STATUS_TOPIC_CREATED = 2;
  public final static int STATUS_PUBLICATION_NOT_CREATED = 3;

  public final static int ERROR_NO_ERROR = -1;
  public final static int ERROR_ERROR = 0;
  public final static int ERROR_NOT_EXISTS_COMPONENT = 1;
  public final static int ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE = 2;
  public final static int ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY = 3;
  public final static int ERROR_INCORRECT_METADATA = 4;
  public final static int ERROR_NOT_EXISTS_TOPIC = 5;
  public final static int ERROR_INCORRECT_AXIS = 6;
  public final static int ERROR_INCORRECT_VALUE = 7;
  public final static int ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT = 8;
  public final static int ERROR_CANT_CREATE_CONTENT = 9;
  public final static int ERROR_CANT_UPDATE_CONTENT = 10;
  public final static int ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT = 11;
  public final static int ERROR_NOT_EXISTS_PUBLICATION_FOR_ID = 12;
  public final static int ERROR_FILE_SIZE_EXCEEDS_LIMIT = 13;

  public UnitReport() {
  }

  public UnitReport(String label) {
    this.label = label;
  }

  /**
   * @return
   */
  public int getError() {
    return error;
  }

  /**
   * @return
   */
  public String getItemName() {
    return itemName;
  }

  /**
   * @return
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param i
   */
  public void setError(int i) {
    error = i;
  }

  /**
   * @param string
   */
  public void setItemName(String string) {
    itemName = string;
  }

  /**
   * @param i
   */
  public void setStatus(int i) {
    status = i;
  }

  public String getLabel() {
    return label;
  }

  /**
   * @param string
   */
  public void setLabel(String string) {
    label = string;
  }
}