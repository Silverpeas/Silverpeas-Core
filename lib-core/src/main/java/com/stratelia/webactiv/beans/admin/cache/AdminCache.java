/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.beans.admin.cache;

import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * The class Store and manage all the Admin's cache
 */
public class AdminCache {
  // Cache management

  static private boolean m_bUseCache = true;
  static private boolean m_bUseSpaceInstCache = true;
  static private Hashtable<String, SpaceInst> m_hSpaceInstCache =
      new Hashtable<String, SpaceInst>();
  static private boolean m_bUseComponentInstCache = true;
  static private Hashtable<String, ComponentInst> m_hComponentInstCache =
      new Hashtable<String, ComponentInst>();
  static private boolean m_bUseProfileInstCache = true;
  static private Hashtable<String, ProfileInst> m_hProfileInstCache =
      new Hashtable<String, ProfileInst>();
  static private boolean m_bUseUserDetailCache = true;
  static private Hashtable<String, UserDetail> m_hUserDetailCache =
      new Hashtable<String, UserDetail>();
  static private boolean m_bUseManageableSpaceIdsCache = true;
  static private Hashtable<String, String[]> m_hManageableSpaceIdsCache =
      new Hashtable<String, String[]>();
  static private boolean m_bUseAvailCompoIdsCache = true;
  static private Hashtable<String, Hashtable<String, String[]>> m_hAvailCompoIdsCache =
      new Hashtable<String, Hashtable<String, String[]>>();
  static private boolean m_bUseProfileIdsCache = true;
  static private Hashtable<String, String[]> m_hProfileIdsCache = new Hashtable<String, String[]>();

  /**
   * Admin Constructor
   */
  public AdminCache() {
  }

  public void setCacheAvailable(boolean useCache) {
    // Cache management
    m_bUseCache = useCache;
  }

  /**
   * Get the number of spaces loaded in cache
   * @return int representing the number of spaces stored in cache
   */
  public int getNbSpacesInCache() {
    return m_hSpaceInstCache.size();
  }

  /**
   * Get the number of components loaded in cache
   * @return int representing the number of components stored in cache
   */
  public int getNbComponentsInCache() {
    return m_hComponentInstCache.size();
  }

  /**
   * Get the number of profiles loaded in cache
   * @return int representing the number of profiles stored in cache
   */
  public int getNbProfilesInCache() {
    return m_hProfileInstCache.size();
  }

  /**
   * Reset data stored in cache
   */
  public void resetCache() {
    SilverTrace.debug("admin", "AdminCache.resetCache",
        "root.MSG_GEN_ENTER_METHOD");
    m_hSpaceInstCache.clear();
    m_hComponentInstCache.clear();
    m_hProfileInstCache.clear();
    m_hUserDetailCache.clear();
    m_hManageableSpaceIdsCache.clear();
    m_hAvailCompoIdsCache.clear();
    m_hProfileIdsCache.clear();
  }

  /*
   * Store the spaceInst in cache
   */
  public void resetSpaceInst() {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      SilverTrace.debug("admin", "AdminCache.resetSpaceInst",
          "root.MSG_GEN_ENTER_METHOD");
      m_hSpaceInstCache.clear();
    }
  }

  public void putSpaceInst(SpaceInst spaceInst) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      SilverTrace.debug("admin", "AdminCache.putSpaceInst",
          "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceInst.getId());
      m_hSpaceInstCache.put(spaceInst.getId(), spaceInst);
    }
  }

  public void removeSpaceInst(String spaceId) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      m_hSpaceInstCache.remove(spaceId);
    }
  }

  public SpaceInst getSpaceInst(String spaceId) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      return m_hSpaceInstCache.get(spaceId);
    } else {
      return null;
    }
  }

  protected void removeUserInSpaceInst(String userId) {
    removeTokenInSpaceInst(userId, false);
  }

  protected void removeGroupInSpaceInst(String groupId) {
    removeTokenInSpaceInst(groupId, true);
  }

  protected void removeTokenInSpaceInst(String tokenId, boolean isGroup) {
    SpaceInst theSpace;
    Iterator<SpaceInst> itSpace = m_hSpaceInstCache.values().iterator();
    Iterator<ComponentInst> itComponent;
    Iterator<ProfileInst> itProfile;
    Iterator<SpaceProfileInst> itSpaceProfile;
    ComponentInst theComponent;

    while (itSpace.hasNext()) {
      theSpace = itSpace.next();
      // First remove it from the SpaceProfileInst
      itSpaceProfile = theSpace.getAllSpaceProfilesInst().iterator();
      while (itSpaceProfile.hasNext()) {
        if (isGroup) {
          itSpaceProfile.next().removeGroup(tokenId);
        } else {
          itSpaceProfile.next().removeUser(tokenId);
        }
      }
      // Second remove it from the ProfileInst of all ComponentInst
      itComponent = theSpace.getAllComponentsInst().iterator();
      while (itComponent.hasNext()) {
        theComponent = itComponent.next();
        itProfile = theComponent.getAllProfilesInst().iterator();
        while (itProfile.hasNext()) {
          if (isGroup) {
            itProfile.next().removeGroup(tokenId);
          } else {
            itProfile.next().removeUser(tokenId);
          }
        }
      }
    }
  }

  /*
   * Store the componentInst in cache
   */
  public void resetComponentInst() {
    if (m_bUseCache && m_bUseComponentInstCache) {
      SilverTrace.debug("admin", "AdminCache.resetComponentInst",
          "root.MSG_GEN_ENTER_METHOD");
      m_hComponentInstCache.clear();
    }
  }

  public void putComponentInst(ComponentInst componentInst) {
    if (m_bUseCache && m_bUseComponentInstCache) {
      SilverTrace.debug("admin", "AdminCache.putComponentInst",
          "root.MSG_GEN_ENTER_METHOD", "ComponentId = "
          + componentInst.getId());
      m_hComponentInstCache.put(componentInst.getId(), componentInst);
    }
  }

  public void removeComponentInst(ComponentInst componentInst) {
    if (m_bUseCache && m_bUseComponentInstCache) {
      SilverTrace.debug("admin", "AdminCache.removeComponentInst",
          "root.MSG_GEN_ENTER_METHOD", "ComponentId = "
          + componentInst.getId());
      m_hComponentInstCache.remove(componentInst.getId());
    }
  }

  public ComponentInst getComponentInst(String componentId) {
    if (m_bUseCache && m_bUseComponentInstCache) {
      return m_hComponentInstCache.get(componentId);
    }
    return null;
  }

  protected void removeUserInComponentInst(String userId) {
    removeTokenInComponentInst(userId, false);
  }

  protected void removeGroupInComponentInst(String groupId) {
    removeTokenInComponentInst(groupId, true);
  }

  protected void removeTokenInComponentInst(String tokenId, boolean isGroup) {
    Iterator<ComponentInst> itComponent = m_hComponentInstCache.values().iterator();
    ComponentInst theComponent;
    Iterator<ProfileInst> itProfile;

    while (itComponent.hasNext()) {
      theComponent = itComponent.next();
      itProfile = theComponent.getAllProfilesInst().iterator();
      while (itProfile.hasNext()) {
        if (isGroup) {
          itProfile.next().removeGroup(tokenId);
        } else {
          itProfile.next().removeUser(tokenId);
        }
      }
    }
  }

  protected void removeSpaceComponentsInst(String spaceId) {
    ComponentInst[] theComponents = m_hComponentInstCache.values().toArray(
        new ComponentInst[m_hComponentInstCache.size()]);

    for (ComponentInst theComponent : theComponents) {
      if (spaceId.equals(getShortSpaceId(theComponent.getDomainFatherId()))) {
        removeComponentsProfilesInst(theComponent.getId());
        removeComponentInst(theComponent);
      }
    }
  }

  /*
   * Store the profileInst in cache
   */
  public void resetProfileInst() {
    if (m_bUseCache && m_bUseProfileInstCache) {
      SilverTrace.debug("admin", "AdminCache.resetProfileInst",
          "root.MSG_GEN_ENTER_METHOD");
      m_hProfileInstCache.clear();
    }
  }

  public void putProfileInst(ProfileInst profileInst) {
    if (m_bUseCache && m_bUseProfileInstCache) {
      SilverTrace.debug("admin", "AdminCache.putProfileInst",
          "root.MSG_GEN_ENTER_METHOD", "ProfileId = " + profileInst.getId());
      m_hProfileInstCache.put(profileInst.getId(), profileInst);
    }
  }

  public void removeProfileInst(ProfileInst profileInst) {
    if (m_bUseCache && m_bUseProfileInstCache) {
      SilverTrace.debug("admin", "AdminCache.removeProfileInst",
          "root.MSG_GEN_ENTER_METHOD", "ProfileId = " + profileInst.getId());
      m_hProfileInstCache.remove(profileInst.getId());
    }
  }

  public ProfileInst getProfileInst(String profileId) {
    if (m_bUseCache && m_bUseProfileInstCache) {
      return m_hProfileInstCache.get(profileId);
    } else {
      return null;
    }
  }

  protected void removeUserInProfileInst(String userId) {
    removeTokenInProfileInst(userId, false);
  }

  protected void removeGroupInProfileInst(String groupId) {
    removeTokenInProfileInst(groupId, true);
  }

  protected void removeTokenInProfileInst(String tokenId, boolean isGroup) {
    Iterator<ProfileInst> itProfile = m_hProfileInstCache.values().iterator();

    while (itProfile.hasNext()) {
      if (isGroup) {
        itProfile.next().removeGroup(tokenId);
      } else {
        itProfile.next().removeUser(tokenId);
      }
    }
  }

  protected void removeComponentsProfilesInst(String componentId) {
    ProfileInst[] theProfiles = m_hProfileInstCache.values().toArray(
        new ProfileInst[m_hProfileInstCache.size()]);

    for (ProfileInst theProfile : theProfiles) {
      if (componentId.equals(theProfile.getComponentFatherId())) {
        removeProfileInst(theProfile);
      }
    }
  }

  /*
   * Store the UserDetail by user
   */
  public void resetUserDetail() {
    SilverTrace.debug("admin", "AdminCache.resetUserDetail",
        "root.MSG_GEN_ENTER_METHOD");
    m_hUserDetailCache.clear();
  }

  public void putUserDetail(String userId, UserDetail userDetail) {
    if (m_bUseCache && m_bUseUserDetailCache) {
      SilverTrace.debug("admin", "AdminCache.putUserDetail",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hUserDetailCache.put(userId, userDetail);
    }
  }

  public void removeUserDetail(String userId) {
    if (m_bUseCache && m_bUseUserDetailCache) {
      SilverTrace.debug("admin", "AdminCache.removeUserDetail",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hUserDetailCache.remove(userId);
    }
  }

  public UserDetail getUserDetail(String userId) {
    if (m_bUseCache && m_bUseUserDetailCache) {
      return m_hUserDetailCache.get(userId);
    } else {
      return null;
    }
  }

  /*
   * Store the ManageableSpaceIds by user
   */
  public void resetManageableSpaceIds() {
    SilverTrace.debug("admin", "AdminCache.resetManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    m_hManageableSpaceIdsCache.clear();
  }

  public void putManageableSpaceIds(String userId, String[] spaceIds) {
    if (m_bUseCache && m_bUseManageableSpaceIdsCache) {
      SilverTrace.debug("admin", "AdminCache.putManageableSpaceIds",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hManageableSpaceIdsCache.put(userId, spaceIds);
    }
  }

  public void removeManageableSpaceIds(String userId) {
    if (m_bUseCache && m_bUseManageableSpaceIdsCache) {
      SilverTrace.debug("admin", "AdminCache.removeManageableSpaceIds",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hManageableSpaceIdsCache.remove(userId);
    }
  }

  public String[] getManageableSpaceIds(String userId) {
    if (m_bUseCache && m_bUseManageableSpaceIdsCache) {
      return m_hManageableSpaceIdsCache.get(userId);
    } else {
      return null;
    }
  }

  /*
   * Store the AvailCompoIds by space and user
   */
  public void resetAvailCompoIds() {
    SilverTrace.debug("admin", "AdminCache.resetAvailCompoIds",
        "root.MSG_GEN_ENTER_METHOD");
    m_hAvailCompoIdsCache.clear();
  }

  public void putAvailCompoIds(String spaceId, String userId, String[] compoIds) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      SilverTrace.debug("admin", "AdminCache.putAvailCompoIds",
          "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + " userId = "
          + userId);
      Hashtable<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(spaceId);
      if (spaceTable == null) {
        spaceTable = new Hashtable<String, String[]>();
        m_hAvailCompoIdsCache.put(spaceId, spaceTable);
      }
      spaceTable.put(userId, compoIds);
    }
  }

  public void removeAvailCompoIds(String spaceId, String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      SilverTrace.debug("admin", "AdminCache.removeAvailCompoIds",
          "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + " userId = "
          + userId);
      Hashtable<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(spaceId);
      if (spaceTable != null) {
        spaceTable.remove(userId);
      }
    }
  }

  public void removeAvailCompoIdsForUser(String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      SilverTrace.debug("admin", "AdminCache.removeAvailCompoIdsForUser",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      Hashtable<String, String[]> spaceTable;
      Iterator<Hashtable<String, String[]>> itHm = m_hAvailCompoIdsCache.values().iterator();
      while (itHm.hasNext()) {
        spaceTable = itHm.next();
        spaceTable.remove(userId);
      }
    }
  }

  public void removeAvailCompoIdsForSpace(String spaceId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      SilverTrace.debug("admin", "AdminCache.removeAvailCompoIdsForSpace",
          "root.MSG_GEN_ENTER_METHOD", "spaceUserId = " + spaceId);
      m_hAvailCompoIdsCache.remove(spaceId);
    }
  }

  public String[] getAvailCompoIds(String spaceId, String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      Hashtable<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(spaceId);
      if (spaceTable != null) {
        return spaceTable.get(userId);
      }
    }
    return null;
  }

  /*
   * Store the ProfileIds by space and user
   */
  public void resetProfileIds() {
    SilverTrace.debug("admin", "AdminCache.resetProfileIds",
        "root.MSG_GEN_ENTER_METHOD");
    m_hProfileIdsCache.clear();
  }

  public void putProfileIds(String userId, String[] profileIds) {
    if (m_bUseCache && m_bUseProfileIdsCache) {
      SilverTrace.debug("admin", "AdminCache.putProfileIds",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hProfileIdsCache.put(userId, profileIds);
    }
  }

  public void removeProfileIds(String userId) {
    if (m_bUseCache && m_bUseProfileIdsCache) {
      SilverTrace.debug("admin", "AdminCache.removeProfileIds",
          "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
      m_hProfileIdsCache.remove(userId);
    }
  }

  public String[] getProfileIds(String userId) {
    if (m_bUseCache && m_bUseProfileIdsCache) {
      return m_hProfileIdsCache.get(userId);
    } else {
      return null;
    }
  }

  /*
   * ----------------------------------------------------------------------------
   * --------------------------------------------------- Operations --------------
   * --------------------------------------------------------------
   * ---------------------------------------------------
   */
  // ----- Spaces -----
  public void opAddSpace(SpaceInst theSpace) {
    if ((theSpace.getDomainFatherId() != null)
        && (theSpace.getDomainFatherId().length() > 0)
        && (!theSpace.getDomainFatherId().equals("0"))) { // This is a subSpace
      // -> Reset the Parent
      // space
      SpaceInst theFather = getSpaceInst(getShortSpaceId(theSpace.getDomainFatherId()));
      if (theFather != null) {
        String[] allChilds = theFather.getSubSpaceIds();
        String[] newChilds;
        if (allChilds == null) {
          allChilds = ArrayUtil.EMPTY_STRING_ARRAY;
        }
        newChilds = new String[allChilds.length + 1];
        for (int i = 0; i < allChilds.length; i++) {
          newChilds[i] = allChilds[i];
        }
        newChilds[allChilds.length] = getShortSpaceId(theSpace.getId());
        theFather.setSubSpaceIds(newChilds);
      }
    }
  }

  public void opUpdateSpace(SpaceInst theSpace) {
    opResetSpace(theSpace);
  }

  public void opRemoveSpace(SpaceInst theSpace) {
    if ((theSpace.getDomainFatherId() != null)
        && (theSpace.getDomainFatherId().length() > 0)
        && (!theSpace.getDomainFatherId().equals("0"))) { // This is a subSpace
      // -> Reset the Parent
      // space
      SpaceInst theFather = getSpaceInst(getShortSpaceId(theSpace.getDomainFatherId()));
      if (theFather != null) {
        String[] allChilds = theFather.getSubSpaceIds();
        String[] newChilds;
        String theSpaceId = getShortSpaceId(theSpace.getId());
        int j = 0;
        newChilds = new String[allChilds.length - 1];
        for (String allChild : allChilds) {
          if (!theSpaceId.equals(allChild)) {
            if (j < allChilds.length - 1) {
              newChilds[j++] = allChild;
            } else { // oups, the child did not exist
              newChilds = allChilds;
            }
          }
        }
        theFather.setSubSpaceIds(newChilds);
      }
    }
    opResetSpace(theSpace);
  }

  protected void opResetSpace(SpaceInst theSpace) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the component and all the child's
    // structs
    removeSpaceComponentsInst(theSpace.getId());
    removeSpaceInst(theSpace.getId());
    resetProfileIds();
    resetAvailCompoIds();
    resetManageableSpaceIds();
  }

  // ----- Components -----
  public void opAddComponent(ComponentInst component) {
    opResetComponent(component);
  }

  public void opUpdateComponent(ComponentInst component) {
    opResetComponent(component);
  }

  public void opRemoveComponent(ComponentInst component) {
    opResetComponent(component);
  }

  protected void opResetComponent(ComponentInst component) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the component and all the child's
    // structs
    removeComponentsProfilesInst(component.getId());
    SpaceInst theSpace = getSpaceInst(getShortSpaceId(component.getDomainFatherId()));
    if (theSpace != null) {
      removeSpaceInst(theSpace.getId());
    }
    removeComponentInst(component);
    resetProfileIds();
    resetAvailCompoIds();
  }

  // ----- Profiles -----
  public void opAddProfile(ProfileInst profile) {
    opResetProfile(profile);
  }

  public void opUpdateProfile(ProfileInst profile) {
    opResetProfile(profile);
  }

  public void opRemoveProfile(ProfileInst profile) {
    opResetProfile(profile);
  }

  protected void opResetProfile(ProfileInst profile) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the profile
    ComponentInst theComponent = getComponentInst(profile.getComponentFatherId());
    if (theComponent != null) {
      SpaceInst theSpace = getSpaceInst(getShortSpaceId(theComponent.getDomainFatherId()));
      if (theSpace != null) {
        removeSpaceInst(theSpace.getId());
      }
      removeComponentInst(theComponent);
    }
    removeProfileInst(profile);
    resetProfileIds();
    resetAvailCompoIds();
  }

  // ----- Space Profiles -----
  public void opAddSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getShortSpaceId(profile.getSpaceFatherId()));
    if (theSpace != null) {
      theSpace.addSpaceProfileInst(profile);
    }
    resetManageableSpaceIds();
  }

  public void opUpdateSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getShortSpaceId(profile.getSpaceFatherId()));
    if (theSpace != null) {
      theSpace.deleteSpaceProfileInst(profile);
      theSpace.addSpaceProfileInst(profile);
    }
    resetManageableSpaceIds();
  }

  public void opRemoveSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getShortSpaceId(profile.getSpaceFatherId()));
    if (theSpace != null) {
      theSpace.deleteSpaceProfileInst(profile);
    }
    resetManageableSpaceIds();
  }

  // ----- Groups -----
  public void opAddGroup(Group group) {
    if (StringUtil.isDefined(group.getSuperGroupId())) { // The group inherits of
      // the permissions of the parent -> too complicated -> reset the permissions of all users
      String[] uids = group.getUserIds();
      for (String uid : uids) {
        opResetUserRights(uid);
      }
    }
  }

  public void opUpdateGroup(Group group) {
    resetProfileIds();
    resetAvailCompoIds();
    resetManageableSpaceIds();
  }

  public void opRemoveGroup(Group group) {
    String groupId = group.getId();

    removeGroupInSpaceInst(groupId);
    removeGroupInComponentInst(groupId);
    removeGroupInProfileInst(groupId);
    resetProfileIds();
    resetAvailCompoIds();
    resetManageableSpaceIds();
  }

  public void opAddUserInGroup(String userId, String groupId) {
    opResetUserRights(userId);
  }

  public void opRemoveUserFromGroup(String userId, String groupId) {
    opResetUserRights(userId);
  }

  // ----- Users -----
  public void opAddUser(UserDetail user) {
  }

  public void opUpdateUser(UserDetail user) {
    removeUserDetail(user.getId());
  }

  public void opRemoveUser(UserDetail user) {
    String userId = user.getId();

    removeUserDetail(userId);
    removeProfileIds(userId);
    removeManageableSpaceIds(userId);
    removeAvailCompoIdsForUser(userId);
    removeUserInSpaceInst(userId);
    removeUserInComponentInst(userId);
    removeUserInProfileInst(userId);
  }

  protected void opResetUserRights(String userId) {
    removeProfileIds(userId);
    removeManageableSpaceIds(userId);
    removeAvailCompoIdsForUser(userId);
  }

  protected String getShortSpaceId(String spaceId) {
    if ((spaceId != null) && (spaceId.startsWith("WA"))) {
      return spaceId.substring(2);
    } else {
      return (spaceId == null) ? "" : spaceId;
    }
  }
}
