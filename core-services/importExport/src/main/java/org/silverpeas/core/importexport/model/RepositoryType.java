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
package org.silverpeas.core.importexport.model;

/**
 * @author tleroi To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RepositoryType {

  public final static int NO_RECURSIVE = 0;
  public final static int RECURSIVE_NOREPLICATE = 1;
  public final static int RECURSIVE_REPLICATE = 2;
  public final static String NO_RECURSIVE_STRING = "NO_RECURSIVE";
  public final static String RECURSIVE_NOREPLICATE_STRING = "RECURSIVE_NOREPLICATE";
  public final static String RECURSIVE_REPLICATE_STRING = "RECURSIVE_REPLICATE";
  private String path;
  private String componentId;
  private int topicId;
  private String massiveType;

  /**
   * @return
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * @return
   */
  public String getMassiveType() {
    return massiveType;
  }

  /**
   * @return
   */
  public int getMassiveTypeInt() {
    if (NO_RECURSIVE_STRING.equals(massiveType)) {
      return NO_RECURSIVE;
    }
    if (RECURSIVE_NOREPLICATE_STRING.equals(massiveType)) {
      return RECURSIVE_NOREPLICATE;
    }
    if (RECURSIVE_REPLICATE_STRING.equals(massiveType)) {
      return RECURSIVE_REPLICATE;
    }
    return -1;
  }

  /**
   * @return
   */
  public String getPath() {
    return path;
  }

  /**
   * @return
   */
  public int getTopicId() {
    return topicId;
  }

  /**
   * @param string
   */
  public void setComponentId(String string) {
    componentId = string;
  }

  /**
   * @param i
   */
  public void setMassiveType(String s) {
    massiveType = s;
  }

  /**
   * @param string
   */
  public void setPath(String string) {
    path = string;
  }

  /**
   * @param i
   */
  public void setTopicId(int i) {
    topicId = i;
  }

}
