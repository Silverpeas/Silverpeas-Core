/*
* Copyright (C) 2000 - 2011 Silverpeas
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
* "http://www.silverpeas.org/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.web.mock;

import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.*;

/**
*
* @author emmanuel.hugonnet@silverpeas.org
*/
public class UserDetailWithProfiles extends UserDetail {

  private static final long serialVersionUID = -7401547950445412571L;
  private final Map<String, List<SilverpeasRole>> profiles = new HashMap<String, List<SilverpeasRole>>();

  /**
* Adds a new profile for tests.
* @param componentId
* @param profile
*/
  public void addProfile(final String componentId, SilverpeasRole profile) {
    if (profiles.containsKey(componentId)) {
      profiles.get(componentId).add(profile);
    } else {
      List<SilverpeasRole> roles = new ArrayList<SilverpeasRole>();
      roles.add(profile);
      profiles.put(componentId, roles);
    }
  }

  /**
* Defines the profiles for tests.
* @param componentId
* @param roles
*/
  public void addProfiles(final String componentId, List<SilverpeasRole> roles) {
    profiles.put(componentId, roles);
  }

  /**
* Clears all of the data used in tests.
*/
  public void clearAll() {
    profiles.clear();
  }

  public String[] getUserProfiles(String componentId) {
    if(profiles.containsKey(componentId)) {
      List<SilverpeasRole> roles = profiles.get(componentId);
      List<String> result = new ArrayList<String>(roles.size());
      for(SilverpeasRole role : roles){
        result.add(role.name());
      }
      return result.toArray(new String[result.size()]);
    }
    return new String[0];
  }
  
  /**
   * Gets the identifier of the Silverpeas components this user can access. A user can access a
   * given component when it plays a defined role in this component; if it has a profile defined
   * for this component.
   * @return an array with the identifier of all of its accessible components.
   */
  public String[] getAccessibleComponentIds() {
    Set<String> componentIds = profiles.keySet();
    return componentIds.toArray(new String[componentIds.size()]);
  }
}