/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class Recover {

  private Admin admin;

  public Recover() {
    admin = AdminReference.getAdminService();
  }

  public void recoverRights() throws AdminException {
    int oldTraceLevel = SilverTrace.getTraceLevel("admin", false);
    SilverTrace.setTraceLevel("admin", SilverTrace.TRACE_LEVEL_WARN);
    String[] rootSpaceIds = admin.getAllRootSpaceIds();
    for (String spaceId : rootSpaceIds) {
      List<SpaceInstLight> subSpaces = admin.getSubSpaces(spaceId);
      for (SpaceInstLight subSpace : subSpaces) {
        recoverSpaceRights(subSpace.getFullId());
      }
    }
    SilverTrace.setTraceLevel("admin", oldTraceLevel);
  }

  public void recoverSpaceRights(String spaceId) throws AdminException {
    recoverSpaceRights(spaceId, true);
  }

  private void recoverSpaceRights(String spaceId, boolean firstAccess) throws AdminException {
    int oldTraceLevel = SilverTrace.TRACE_LEVEL_ERROR;
    if (firstAccess) {
      oldTraceLevel = SilverTrace.getTraceLevel("admin", false);
      SilverTrace.setTraceLevel("admin", SilverTrace.TRACE_LEVEL_WARN);
    }
    // check if space do not inherit rights but still have inherit rights
    SpaceInst space = admin.getSpaceInstById(spaceId);
    if (!space.isRoot()) {
      if (space.isInheritanceBlocked()) {
        List<SpaceProfileInst> profiles = space.getInheritedProfiles();
        if (!profiles.isEmpty()) {
          // an inconsistency have been detected
          SilverTrace.warn("admin", "Recover.recoverSpaceRights", space.getName() +
              " does not inherit rights but still had inherit rights");
          // delete all inherited profiles
          for (SpaceProfileInst profile : profiles) {
            admin.deleteSpaceProfileInst(profile.getId(), null);
          }
        }
      } else {
        // recover space rights (those inherited from parent)
        admin.setSpaceProfilesToSubSpace(space, null, true, true);
      }
    }

    // recover subspaces rights
    String[] subSpaceIds = space.getSubSpaceIds();
    for (String subSpaceId : subSpaceIds) {
      recoverSpaceRights(subSpaceId, false);
    }

    // recover components rights
    List<ComponentInst> components = space.getAllComponentsInst();
    for (ComponentInst component : components) {
      recoverComponentRights(component, space);
    }

    if (firstAccess) {
      SilverTrace.setTraceLevel("admin", oldTraceLevel);
    }
  }

  private void recoverComponentRights(ComponentInst component, SpaceInst space)
      throws AdminException {
    // check if component does not inherit rights but still have inherit rights
    if (component.isInheritanceBlocked()) {
      List<ProfileInst> profiles = component.getInheritedProfiles();
      if (!profiles.isEmpty()) {
        // an inconsistency have been detected
        SilverTrace.warn("admin", "Recover.recoverComponentRights", component.getLabel() +
            " does not inherit rights but still had inherit rights");
        // delete all inherited profiles
        for (ProfileInst profile : profiles) {
          admin.deleteProfileInst(profile.getId(), null);
        }
      }
    } else {
      // recover space rights
      admin.setSpaceProfilesToComponent(component, space, true);
    }
  }
}