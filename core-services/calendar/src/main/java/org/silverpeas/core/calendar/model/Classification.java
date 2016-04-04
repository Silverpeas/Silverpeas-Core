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
package org.silverpeas.core.calendar.model;

import java.io.Serializable;

public class Classification implements Serializable {

  private static final long serialVersionUID = 8420742110510656934L;
  final public static String PRIVATE = "private";
  final public static String PUBLIC = "public";
  final public static String CONFIDENTIAL = "confidential";

  static public String[] getAllClassifications() {
    String[] all = {PUBLIC, PRIVATE, CONFIDENTIAL};
    return all;
  }

  static public String[] getAllClassificationsWithoutConfidential() {
    String[] all = {PUBLIC, PRIVATE};
    return all;
  }
  private String classification = PRIVATE;

  public Classification() {
  }

  public Classification(String classification) {
    setString(classification);
  }

  public void setString(String classification) {
    if (classification == null) {
      return;
    }
    if (PRIVATE.equals(classification)) {
      this.classification = PRIVATE;
    } else if (PUBLIC.equals(classification)) {
      this.classification = PUBLIC;
    } else if (CONFIDENTIAL.equals(classification)) {
      this.classification = CONFIDENTIAL;
    }
  }

  public String getString() {
    return classification;
  }

  public boolean isPublic() {
    return PUBLIC.equals(classification);
  }

  public boolean isPrivate() {
    return PRIVATE.equals(classification);
  }

  public boolean isConfidential() {
    return CONFIDENTIAL.equals(classification);
  }
}