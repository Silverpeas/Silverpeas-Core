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

package org.silverpeas.core.admin.service.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * The class Store and manage all the Admin's cache
 */
public class AdminCache {
  // Cache management

  static private boolean m_bUseCache = true;
  static private boolean m_bUseSpaceInstCache = true;
  static private Map<Integer, SpaceInst> m_hSpaceInstCache
      = new ConcurrentHashMap<>();
  static private boolean m_bUseComponentInstCache = true;
  static private Map<Integer, ComponentInst> m_hComponentInstCache
      = new ConcurrentHashMap<>();
  static private boolean m_bUseProfileInstCache = true;
  static private Map<String, ProfileInst> m_hProfileInstCache
      = new ConcurrentHashMap<String, ProfileInst>();
  static private boolean m_bUseUserDetailCache = true;
  static private Map<String, UserDetail> m_hUserDetailCache
      = new ConcurrentHashMap<String, UserDetail>();
  static private boolean m_bUseManageableSpaceIdsCache = true;
  static private Map<String, Integer[]> m_hManageableSpaceIdsCache
      = new ConcurrentHashMap<>();
  static private boolean m_bUseAvailCompoIdsCache = true;
  static private Map<String, Map<String, String[]>> m_hAvailCompoIdsCache
      = new ConcurrentHashMap<String, Map<String, String[]>>();
  static private boolean m_bUseProfileIdsCache = true;
  static private Map<String, String[]> m_hProfileIdsCache
      = new ConcurrentHashMap<String, String[]>();
  static private boolean m_bUseNodeProfileIdsCache = true;
  static private Map<String, String[]> m_hNodeProfileIdsCache
          = new ConcurrentHashMap<String, String[]>();

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
   *
   * @return int representing the number of spaces stored in cache
   */
  public int getNbSpacesInCache() {
    return m_hSpaceInstCache.size();
  }

  /**
   * Get the number of components loaded in cache
   *
   * @return int representing the number of components stored in cache
   */
  public int getNbComponentsInCache() {
    return m_hComponentInstCache.size();
  }

  /**
   * Get the number of profiles loaded in cache
   *
   * @return int representing the number of profiles stored in cache
   */
  public int getNbProfilesInCache() {
    return m_hProfileInstCache.size();
  }

  /**
   * Reset data stored in cache
   */
  public void resetCache() {
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
      m_hSpaceInstCache.clear();
    }
  }

  public void putSpaceInst(SpaceInst spaceInst) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      m_hSpaceInstCache.put(spaceInst.getLocalId(), spaceInst);
    }
  }

  public void removeSpaceInst(int spaceId) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      m_hSpaceInstCache.remove(spaceId);
    }
  }

  public SpaceInst getSpaceInst(int spaceId) {
    if (m_bUseCache && m_bUseSpaceInstCache) {
      return m_hSpaceInstCache.get(spaceId);
    }
    return null;
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
      m_hComponentInstCache.clear();
    }
  }

  public void putComponentInst(ComponentInst componentInst) {
    if (m_bUseCache && m_bUseComponentInstCache) {
      m_hComponentInstCache.put(componentInst.getLocalId(), componentInst);
    }
  }

  public void removeComponentInst(ComponentInst componentInst) {
    if (m_bUseCache && m_bUseComponentInstCache) {
      m_hComponentInstCache.remove(componentInst.getLocalId());
    }
  }

  public ComponentInst getComponentInst(int componentId) {
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

  protected void removeSpaceComponentsInst(int spaceId) {
    ComponentInst[] theComponents = m_hComponentInstCache.values().toArray(
        new ComponentInst[m_hComponentInstCache.size()]);

    for (ComponentInst theComponent : theComponents) {
      final Integer localSpaceId = getLocalSpaceId(theComponent.getDomainFatherId());
      if (localSpaceId != null && spaceId == localSpaceId) {
        removeComponentsProfilesInst(theComponent.getLocalId());
        removeComponentInst(theComponent);
      }
    }
  }

  /*
   * Store the profileInst in cache
   */
  public void resetProfileInst() {
    if (m_bUseCache && m_bUseProfileInstCache) {
      m_hProfileInstCache.clear();
    }
  }

  public void putProfileInst(ProfileInst profileInst) {
    if (m_bUseCache && m_bUseProfileInstCache) {
      m_hProfileInstCache.put(profileInst.getId(), profileInst);
    }
  }

  public void removeProfileInst(ProfileInst profileInst) {
    if (m_bUseCache && m_bUseProfileInstCache) {
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

  protected void removeComponentsProfilesInst(int componentId) {
    ProfileInst[] theProfiles = m_hProfileInstCache.values().toArray(
        new ProfileInst[m_hProfileInstCache.size()]);

    for (ProfileInst theProfile : theProfiles) {
      if (String.valueOf(componentId).equals(theProfile.getComponentFatherId())) {
        removeProfileInst(theProfile);
      }
    }
  }

  /*
   * Store the UserDetail by user
   */
  public void resetUserDetail() {
    m_hUserDetailCache.clear();
  }

  public void putUserDetail(String userId, UserDetail userDetail) {
    if (m_bUseCache && m_bUseUserDetailCache) {
      m_hUserDetailCache.put(userId, userDetail);
    }
  }

  public void removeUserDetail(String userId) {
    if (m_bUseCache && m_bUseUserDetailCache) {
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
    m_hManageableSpaceIdsCache.clear();
  }

  public void putManageableSpaceIds(String userId, Integer[] spaceIds) {
    if (m_bUseCache && m_bUseManageableSpaceIdsCache) {
      m_hManageableSpaceIdsCache.put(userId, spaceIds);
    }
  }

  public void removeManageableSpaceIds(String userId) {
    if (m_bUseCache && m_bUseManageableSpaceIdsCache) {
      m_hManageableSpaceIdsCache.remove(userId);
    }
  }

  public Integer[] getManageableSpaceIds(String userId) {
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
    m_hAvailCompoIdsCache.clear();
  }

  public void putAvailCompoIds(String spaceId, String userId, String[] compoIds) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      Map<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(spaceId);
      if (spaceTable == null) {
        spaceTable = new ConcurrentHashMap<String, String[]>();
        m_hAvailCompoIdsCache.put(spaceId, spaceTable);
      }
      spaceTable.put(userId, compoIds);
    }
  }

  public void removeAvailCompoIds(String spaceId, String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      Map<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(spaceId);
      if (spaceTable != null) {
        spaceTable.remove(userId);
      }
    }
  }

  public void removeAvailCompoIdsForUser(String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      for (Map<String, String[]> spaceTable : m_hAvailCompoIdsCache.values()) {
        spaceTable.remove(userId);
      }
    }
  }

  public void removeAvailCompoIdsForSpace(String spaceId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      m_hAvailCompoIdsCache.remove(spaceId);
    }
  }

  public String[] getAvailCompoIds(int spaceId, String userId) {
    if (m_bUseCache && m_bUseAvailCompoIdsCache) {
      Map<String, String[]> spaceTable = m_hAvailCompoIdsCache.get(String.valueOf(spaceId));
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
    m_hProfileIdsCache.clear();
  }

  public void putProfileIds(String userId, String[] profileIds) {
    if (m_bUseCache && m_bUseProfileIdsCache) {
      m_hProfileIdsCache.put(userId, profileIds);
    }
  }

  public void putNodeProfileIds(String userId, String[] profileIds) {
    if (m_bUseCache && m_bUseNodeProfileIdsCache) {
      m_hNodeProfileIdsCache.put(userId, profileIds);
    }
  }

  public void removeProfileIds(String userId) {
    if (m_bUseCache && m_bUseProfileIdsCache) {
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

  public String[] getNodeProfileIds(String userId) {
    if (m_bUseCache && m_bUseNodeProfileIdsCache) {
      return m_hNodeProfileIdsCache.get(userId);
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
      SpaceInst theFather = getSpaceInst(getLocalSpaceId(theSpace.getDomainFatherId()));
      if (theFather != null) {
        List<SpaceInst> subSpaces = new ArrayList<>(theFather.getSubSpaces());
        subSpaces.add(theSpace);
        theFather.setSubSpaces(subSpaces);
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
      SpaceInst theFather = getSpaceInst(getLocalSpaceId(theSpace.getDomainFatherId()));
      if (theFather != null) {
        List<SpaceInst> subSpaces = new ArrayList<>(theFather.getSubSpaces());
        for (int i = 0; i < subSpaces.size(); i++) {
          if (subSpaces.get(i).getLocalId() == theSpace.getLocalId()) {
            subSpaces.remove(i);
            break;
          }
        }
        theFather.setSubSpaces(subSpaces);
      }
    }
    opResetSpace(theSpace);
  }

  protected void opResetSpace(SpaceInst theSpace) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the component and all the child's
    // structs
    removeSpaceComponentsInst(theSpace.getLocalId());
    removeSpaceInst(theSpace.getLocalId());
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
    removeComponentsProfilesInst(component.getLocalId());
    SpaceInst theSpace = getSpaceInst(getLocalSpaceId(component.getDomainFatherId()));
    if (theSpace != null) {
      removeSpaceInst(theSpace.getLocalId());
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
    ComponentInst theComponent = getComponentInst(Integer.parseInt(profile.getComponentFatherId()));
    if (theComponent != null) {
      SpaceInst theSpace = getSpaceInst(getLocalSpaceId(theComponent.getDomainFatherId()));
      if (theSpace != null) {
        removeSpaceInst(theSpace.getLocalId());
      }
      removeComponentInst(theComponent);
    }
    removeProfileInst(profile);
    resetProfileIds();
    resetAvailCompoIds();
  }

  // ----- Space Profiles -----
  public void opAddSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getLocalSpaceId(profile.getSpaceFatherId()));
    if (theSpace != null) {
      theSpace.addSpaceProfileInst(profile);
    }
    resetManageableSpaceIds();
  }

  public void opUpdateSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getLocalSpaceId(profile.getSpaceFatherId()));
    if (theSpace != null) {
      theSpace.deleteSpaceProfileInst(profile);
      theSpace.addSpaceProfileInst(profile);
    }
    resetManageableSpaceIds();
  }

  public void opRemoveSpaceProfile(SpaceProfileInst profile) {
    SpaceInst theSpace = getSpaceInst(getLocalSpaceId(profile.getSpaceFatherId()));
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

  protected Integer getLocalSpaceId(String spaceId) {
    boolean isSpaceIdDefined = StringUtil.isDefined(spaceId);
    if (isSpaceIdDefined && (spaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX))) {
      return Integer.parseInt(spaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length()));
    } else {
      return !isSpaceIdDefined ? null : Integer.parseInt(spaceId);
    }
  }
}
