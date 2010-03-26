/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.jaas;

public class SilverpeasUserProfileEntry {

  public static final String READER_PROFILE = "reader";
  public static final String SUBSCRIBER_PROFILE = "subscriber";
  public static final String MODERATOR_PROFILE = "moderator";
  public static final String ROOT_PROFILE = "admin";
  public static final String PUBLISHER_PROFILE = "publisher";
  public static final String WRITER_PROFILE = "writer";
  public static final String USER_PROFILE = "user";
  private String componentId;

  private String profile;

  public SilverpeasUserProfileEntry(String componentId, String profile) {
    super();
    this.componentId = componentId;
    this.profile = profile;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getProfile() {
    return profile;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((componentId == null) ? 0 : componentId.hashCode());
    result = prime * result + ((profile == null) ? 0 : profile.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final SilverpeasUserProfileEntry other = (SilverpeasUserProfileEntry) obj;
    if (componentId == null) {
      if (other.componentId != null)
        return false;
    } else if (!componentId.equals(other.componentId))
      return false;
    if (profile == null) {
      if (other.profile != null)
        return false;
    } else if (!profile.equals(other.profile))
      return false;
    return true;
  }

}
