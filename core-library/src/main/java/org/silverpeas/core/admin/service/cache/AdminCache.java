/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.service.cache;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class Store and manage all the Admin's cache
 */
@Singleton
public class AdminCache {
  // Cache management

  private boolean useCache = true;
  private boolean useSpaceInstCache = true;
  private Map<Integer, SpaceInst> spaceInstCache = new ConcurrentHashMap<>();
  private boolean useComponentInstCache = true;
  private Map<Integer, ComponentInst> componentInstCache = new ConcurrentHashMap<>();
  private boolean useProfileInstCache = true;
  private Map<String, ProfileInst> profileInstCache = new ConcurrentHashMap<>();
  private boolean useUserDetailCache = true;
  private Map<String, UserDetail> userDetailCache = new ConcurrentHashMap<>();
  private boolean useManageableSpaceIdsCache = true;
  private Map<String, Integer[]> manageableSpaceIdsCache = new ConcurrentHashMap<>();
  private boolean useAvailCompoIdsCache = true;
  private Map<String, Map<String, String[]>> availCompoIdsCache = new ConcurrentHashMap<>();
  private boolean useProfileIdsCache = true;
  private Map<String, String[]> profileIdsCache = new ConcurrentHashMap<>();

  public void setCacheAvailable(boolean useCache) {
    // Cache management
    this.useCache = useCache;
  }

  /**
   * Reset data stored in cache
   */
  public void resetCache() {
    spaceInstCache.clear();
    componentInstCache.clear();
    profileInstCache.clear();
    userDetailCache.clear();
    manageableSpaceIdsCache.clear();
    availCompoIdsCache.clear();
    profileIdsCache.clear();
  }

  /*
   * Store the spaceInst in cache
   */
  public void resetSpaceInst() {
    if (useCache && useSpaceInstCache) {
      spaceInstCache.clear();
    }
  }

  public void putSpaceInst(SpaceInst spaceInst) {
    if (useCache && useSpaceInstCache) {
      spaceInstCache.put(spaceInst.getLocalId(), spaceInst);
    }
  }

  public void removeSpaceInst(int spaceId) {
    if (useCache && useSpaceInstCache) {
      spaceInstCache.remove(spaceId);
    }
  }

  public Optional<SpaceInst> getSpaceInst(int spaceId) {
    if (useCache && useSpaceInstCache) {
      return Optional.ofNullable(spaceInstCache.get(spaceId));
    }
    return Optional.empty();
  }

  private void removeUserInSpaceInst(String userId) {
    removeTokenInSpaceInst(userId, false);
  }

  private void removeGroupInSpaceInst(String groupId) {
    removeTokenInSpaceInst(groupId, true);
  }

  private void removeTokenInSpaceInst(String tokenId, boolean isGroup) {
    for (final SpaceInst theSpace : spaceInstCache.values()) {
      // First remove it from the SpaceProfileInst
      for (final SpaceProfileInst spaceProfile : theSpace.getAllSpaceProfilesInst()) {
        if (isGroup) {
          spaceProfile.removeGroup(tokenId);
        } else {
          spaceProfile.removeUser(tokenId);
        }
      }
      // Second remove it from the ProfileInst of all ComponentInst
      removeInProfilesForAllComponentInstances(tokenId, isGroup, theSpace.getAllComponentsInst());
    }
  }

  private void removeInProfilesForAllComponentInstances(final String tokenId, final boolean isGroup,
      final Collection<ComponentInst> componentInstances) {
    for (final ComponentInst theComponent : componentInstances) {
      for (final ProfileInst theProfile : theComponent.getAllProfilesInst()) {
        if (isGroup) {
          theProfile.removeGroup(tokenId);
        } else {
          theProfile.removeUser(tokenId);
        }
      }
    }
  }

  /*
   * Store the componentInst in cache
   */
  public void resetComponentInst() {
    if (useCache && useComponentInstCache) {
      componentInstCache.clear();
    }
  }

  public void putComponentInst(ComponentInst componentInst) {
    if (useCache && useComponentInstCache) {
      componentInstCache.put(componentInst.getLocalId(), componentInst);
    }
  }

  private void removeComponentInst(ComponentInst componentInst) {
    if (useCache && useComponentInstCache) {
      componentInstCache.remove(componentInst.getLocalId());
    }
  }

  public Optional<ComponentInst> getComponentInst(int componentId) {
    if (useCache && useComponentInstCache) {
      return Optional.ofNullable(componentInstCache.get(componentId));
    }
    return Optional.empty();
  }

  private void removeUserInComponentInst(String userId) {
    removeTokenInComponentInst(userId, false);
  }

  private void removeGroupInComponentInst(String groupId) {
    removeTokenInComponentInst(groupId, true);
  }

  private void removeTokenInComponentInst(String tokenId, boolean isGroup) {
    removeInProfilesForAllComponentInstances(tokenId, isGroup, componentInstCache.values());
  }

  private void removeSpaceComponentsInst(int spaceId) {
    final Collection<ComponentInst> theComponents = componentInstCache.values();
    for (ComponentInst theComponent : theComponents) {
      final Integer localSpaceId = getLocalSpaceId(theComponent.getDomainFatherId());
      if (localSpaceId != null && spaceId == localSpaceId) {
        removeComponentsProfilesInst(theComponent.getLocalId());
        removeComponentInst(theComponent);
      }
    }
  }

  public void putProfileInst(ProfileInst profileInst) {
    if (useCache && useProfileInstCache) {
      profileInstCache.put(profileInst.getId(), profileInst);
    }
  }

  private void removeProfileInst(ProfileInst profileInst) {
    if (useCache && useProfileInstCache) {
      profileInstCache.remove(profileInst.getId());
    }
  }

  public Optional<ProfileInst> getProfileInst(String profileId) {
    if (useCache && useProfileInstCache) {
      return Optional.ofNullable(profileInstCache.get(profileId));
    } else {
      return Optional.empty();
    }
  }

  private void removeUserInProfileInst(String userId) {
    removeTokenInProfileInst(userId, false);
  }

  private void removeGroupInProfileInst(String groupId) {
    removeTokenInProfileInst(groupId, true);
  }

  private void removeTokenInProfileInst(String tokenId, boolean isGroup) {
    final Collection<ProfileInst> profiles = profileInstCache.values();
    for (final ProfileInst profile : profiles) {
      if (isGroup) {
        profile.removeGroup(tokenId);
      } else {
        profile.removeUser(tokenId);
      }
    }
  }

  private void removeComponentsProfilesInst(int componentId) {
    final Collection<ProfileInst> theProfiles = profileInstCache.values();
    for (ProfileInst theProfile : theProfiles) {
      if (String.valueOf(componentId).equals(theProfile.getComponentFatherId())) {
        removeProfileInst(theProfile);
      }
    }
  }

  public void putUserDetail(String userId, UserDetail userDetail) {
    if (useCache && useUserDetailCache) {
      userDetailCache.put(userId, userDetail);
    }
  }

  private void removeUserDetail(String userId) {
    if (useCache && useUserDetailCache) {
      userDetailCache.remove(userId);
    }
  }

  public Optional<UserDetail> getUserDetail(String userId) {
    if (useCache && useUserDetailCache) {
      return Optional.ofNullable(userDetailCache.get(userId));
    } else {
      return Optional.empty();
    }
  }

  /*
   * Store the ManageableSpaceIds by user
   */
  private void resetManageableSpaceIds() {
    manageableSpaceIdsCache.clear();
  }

  public void putManageableSpaceIds(String userId, Integer[] spaceIds) {
    if (useCache && useManageableSpaceIdsCache) {
      manageableSpaceIdsCache.put(userId, spaceIds);
    }
  }

  private void removeManageableSpaceIds(String userId) {
    if (useCache && useManageableSpaceIdsCache) {
      manageableSpaceIdsCache.remove(userId);
    }
  }

  public Optional<Integer[]> getManageableSpaceIds(String userId) {
    if (useCache && useManageableSpaceIdsCache) {
      return Optional.ofNullable(manageableSpaceIdsCache.get(userId));
    } else {
      return Optional.empty();
    }
  }

  /*
   * Store the AvailCompoIds by space and user
   */
  private void resetAvailCompoIds() {
    availCompoIdsCache.clear();
  }

  public void putAvailCompoIds(String spaceId, String userId, String[] compoIds) {
    if (useCache && useAvailCompoIdsCache) {
      final Map<String, String[]> spaceTable =
          availCompoIdsCache.computeIfAbsent(spaceId, s -> new ConcurrentHashMap<>());
      spaceTable.put(userId, compoIds);
    }
  }

  private void removeAvailCompoIdsForUser(String userId) {
    if (useCache && useAvailCompoIdsCache) {
      for (Map<String, String[]> spaceTable : availCompoIdsCache.values()) {
        spaceTable.remove(userId);
      }
    }
  }

  public Optional<String[]> getAvailCompoIds(int spaceId, String userId) {
    if (useCache && useAvailCompoIdsCache) {
      Map<String, String[]> spaceTable = availCompoIdsCache.get(String.valueOf(spaceId));
      if (spaceTable != null) {
        return Optional.ofNullable(spaceTable.get(userId));
      }
    }
    return Optional.empty();
  }

  /*
   * Store the ProfileIds by space and user
   */
  private void resetProfileIds() {
    profileIdsCache.clear();
  }

  public void putProfileIds(String userId, String[] profileIds) {
    if (useCache && useProfileIdsCache) {
      profileIdsCache.put(userId, profileIds);
    }
  }

  private void removeProfileIds(String userId) {
    if (useCache && useProfileIdsCache) {
      profileIdsCache.remove(userId);
    }
  }

  public Optional<String[]> getProfileIds(String userId) {
    if (useCache && useProfileIdsCache) {
      return Optional.ofNullable(profileIdsCache.get(userId));
    } else {
      return Optional.empty();
    }
  }

  /*
   * ----------------------------------------------------------------------------
   * --------------------------------------------------- Operations --------------
   * --------------------------------------------------------------
   * ---------------------------------------------------
   */
  // ----- Spaces -----
  public void opAddSpace(final SpaceInst theSpace) {
    if ((theSpace.getDomainFatherId() != null)
        && (theSpace.getDomainFatherId().length() > 0)
        && (!theSpace.getDomainFatherId().equals("0"))) { // This is a subSpace
      // -> Reset the Parent
      // space
      Integer spaceId = getLocalSpaceId(theSpace.getDomainFatherId());
      if (spaceId != null) {
        Optional<SpaceInst> optionalFather = getSpaceInst(spaceId);
        optionalFather.ifPresent(f -> {
          final List<SpaceInst> subSpaces = new ArrayList<>(f.getSubSpaces());
          subSpaces.add(theSpace);
          f.setSubSpaces(subSpaces);
        });
      }
    }
  }

  public void opUpdateSpace(SpaceInst theSpace) {
    opResetSpace(theSpace);
  }

  public void opRemoveSpace(final SpaceInst theSpace) {
    if ((theSpace.getDomainFatherId() != null)
        && (theSpace.getDomainFatherId().length() > 0)
        && (!theSpace.getDomainFatherId().equals("0"))) { // This is a subSpace
      // -> Reset the Parent
      // space
      Integer spaceId = getLocalSpaceId(theSpace.getDomainFatherId());
      if (spaceId != null) {
        Optional<SpaceInst> theFather = getSpaceInst(spaceId);
        theFather.ifPresent(f -> {
          final List<SpaceInst> subSpaces = new ArrayList<>(f.getSubSpaces());
          subSpaces.stream()
              .filter(s -> s.getLocalId() == theSpace.getLocalId())
              .findFirst()
              .ifPresent(subSpaces::remove);
          f.setSubSpaces(subSpaces);
        });
      }
    }
    opResetSpace(theSpace);
  }

  private void opResetSpace(SpaceInst theSpace) {
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

  private void opResetComponent(ComponentInst component) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the component and all the child's
    // structs
    removeComponentsProfilesInst(component.getLocalId());
    removeComponent(component);
    resetProfileIds();
    resetAvailCompoIds();
  }

  private void removeComponent(final ComponentInst component) {
    Integer spaceId = getLocalSpaceId(component.getDomainFatherId());
    if (spaceId != null) {
      Optional<SpaceInst> theSpace = getSpaceInst(spaceId);
      theSpace.ifPresent(s -> removeSpaceInst(s.getLocalId()));
    }
    removeComponentInst(component);
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

  private void opResetProfile(ProfileInst profile) {
    // First level cache reset : it's not the best but it's simple : remove all
    // structs from cache that includes the profile
    int compoLocalId = ComponentInst.getComponentLocalId(profile.getComponentFatherId());
    Optional<ComponentInst> theComponent = getComponentInst(compoLocalId);
    theComponent.ifPresent(this::removeComponent);
    removeProfileInst(profile);
    resetProfileIds();
    resetAvailCompoIds();
  }

  // ----- Space Profiles -----
  public void opAddSpaceProfile(final SpaceProfileInst profile) {
    Integer spaceId = getLocalSpaceId(profile.getSpaceFatherId());
    if (spaceId != null) {
      Optional<SpaceInst> theSpace = getSpaceInst(spaceId);
      theSpace.ifPresent(s -> s.addSpaceProfileInst(profile));
    }
    resetManageableSpaceIds();
  }

  public void opUpdateSpaceProfile(final SpaceProfileInst profile) {
    Integer spaceId = getLocalSpaceId(profile.getSpaceFatherId());
    if (spaceId != null) {
      Optional<SpaceInst> theSpace = getSpaceInst(spaceId);
      theSpace.ifPresent(s -> {
        s.deleteSpaceProfileInst(profile);
        s.addSpaceProfileInst(profile);
      });
    }
    resetManageableSpaceIds();
  }

  public void opRemoveSpaceProfile(final SpaceProfileInst profile) {
    Integer spaceId = getLocalSpaceId(profile.getSpaceFatherId());
    if (spaceId != null) {
      Optional<SpaceInst> theSpace = getSpaceInst(spaceId);
      theSpace.ifPresent(s -> s.deleteSpaceProfileInst(profile));
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

  public void resetOnUpdateGroup() {
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

  public void opAddUserInGroup(String userId) {
    opResetUserRights(userId);
  }

  public void opRemoveUserFromGroup(String userId) {
    opResetUserRights(userId);
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

  private void opResetUserRights(String userId) {
    removeProfileIds(userId);
    removeManageableSpaceIds(userId);
    removeAvailCompoIdsForUser(userId);
  }

  private Integer getLocalSpaceId(String spaceId) {
    boolean isSpaceIdDefined = StringUtil.isDefined(spaceId);
    if (isSpaceIdDefined && (spaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX))) {
      return Integer.parseInt(spaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length()));
    } else {
      return !isSpaceIdDefined ? null : Integer.parseInt(spaceId);
    }
  }
}
