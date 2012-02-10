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