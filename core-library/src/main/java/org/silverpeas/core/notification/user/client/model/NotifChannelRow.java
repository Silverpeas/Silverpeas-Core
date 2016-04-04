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

package org.silverpeas.core.notification.user.client.model;

public class NotifChannelRow {
  private int id;
  private String name;
  private String description;
  private String couldBeAdded;
  private String fromAvailable;
  private String subjectAvailable;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public String getName() {
    return name;
  }

  public void setName(String aName) {
    name = aName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String aDescription) {
    description = aDescription;
  }

  public String getCouldBeAdded() {
    return couldBeAdded;
  }

  public void setCouldBeAdded(String aCouldBeAdded) {
    couldBeAdded = aCouldBeAdded;
  }

  public String getFromAvailable() {
    return fromAvailable;
  }

  public void setFromAvailable(String aFromAvailable) {
    fromAvailable = aFromAvailable;
  }

  public String getSubjectAvailable() {
    return subjectAvailable;
  }

  public void setSubjectAvailable(String aSubjectAvailable) {
    subjectAvailable = aSubjectAvailable;
  }

  public NotifChannelRow(int aId, String aName, String aDescription,
      String aCouldBeAdded, String aFromAvailable, String aSubjectAvailable) {
    id = aId;
    name = aName;
    description = aDescription;
    couldBeAdded = aCouldBeAdded;
    fromAvailable = aFromAvailable;
    subjectAvailable = aSubjectAvailable;
  }
}
