/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * A service to ease the management of the members of a community for the applications that are in
 * charge of the community membership. This is for those applications of having to use explicitly
 * the {@link org.silverpeas.core.admin.service.Administration} service for doing.
 *
 * @author mmoquillon
 */
@Service
@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class CommunityMembershipService {

  @Inject
  private Administration admin;

  /**
   * Gets an instance of a {@link CommunityMembershipService}.
   *
   * @return an {@link CommunityMembershipService} instance.
   */
  public static CommunityMembershipService get() {
    return ServiceProvider.getService(CommunityMembershipService.class);
  }

  /**
   * Gets the community identified by the specified community space unique identifier.
   *
   * @param spaceId the unique identifier of a community space.
   * @return a {@link SpaceInst} instance that represents the asked community.
   * @throws AdminException if the community space cannot be found.
   */
  public SpaceInst getCommunity(String spaceId) throws AdminException {
    SpaceInst space = admin.getSpaceInstById(spaceId);
    if (space == null || !space.isCommunitySpace()) {
      throw new AdminException("No community space found with id " + spaceId);
    }
    return space;
  }

  /**
   * Gets the community members group identified by the specified identifier. If no such group
   * exists, null is returned. If the group isn't a community one, an
   * {@link IllegalArgumentException} is thrown.
   *
   * @param groupId the unique identifier of a user group
   * @return an application managed user group.
   * @throws AdminException if an error occurs while fetching the asked user group
   * @throws IllegalArgumentException if the asked group isn't managed by a Silverpeas application.
   */
  public CommunityMembersGroup getGroup(String groupId) throws AdminException {
    return asAppManagedGroupDetail(admin.getGroup(groupId));
  }

  /**
   * Saves any changes in the specified user group in the datasource.
   *
   * @param group the user group.
   * @throws AdminException if the update fails.
   */
  public void updateGroup(CommunityMembersGroup group) throws AdminException {
    admin.updateGroup(group, true);
  }

  /**
   * Adds the specified user as a member of the community represented by the specified members group
   * and with the specified role to play in the community.
   *
   * @param user the user to add among the members of a community.
   * @param group the group of members associated with the community space.
   * @param role the role the user has to play in the community space.
   * @throws AdminException if an error occurs while putting the user as a member of the community
   * space.
   */
  public void addMember(User user, CommunityMembersGroup group, SilverpeasRole role)
      throws AdminException {
    User currentUser = User.getCurrentRequester();
    var space = admin.getSpaceInstById(group.getSpaceId());
    var profile = getSpaceProfile(currentUser, role, space);
    if (!isUserHasProfile(user, profile)) {
      profile.addUser(user.getId());
      admin.updateSpaceProfileInst(profile, currentUser.getId());
    }
    if (!isUserInGroup(user, group)) {
      admin.addUserInGroup(user.getId(), group.getId());
    }
  }

  /**
   * Removes the specified user from the members of the community represented by the specified
   * members group. The consequence is the user isn't anymore a member of the community space
   * related by the given group.
   *
   * @param user the user to remove from the community.
   * @param group the group of members of the community.
   * @throws AdminException if an error occurs while removing the user from the community.
   */
  public void removeMember(User user, CommunityMembersGroup group) throws AdminException {
    var space = admin.getSpaceInstById(group.getSpaceId());
    removeUserFromAllSpaceProfiles(user, space);
    if (isUserInGroup(user, group)) {
      admin.removeUserFromGroup(user.getId(), group.getId());
    }
  }

  /**
   * Setups the community in Silverpeas for the specified community space. The given space has to
   * be a community one, otherwise an {@link IllegalArgumentException} will be thrown. A group of
   * members of the specified community will be created with, optionally, the given symbol as a
   * prefix of the group name.
   *
   * @param spaceId the unique identifier of a community space.
   * @param symbol a symbol to prepend to the name of the members group in order to distinguish it.
   * @return the group with which the community members can be handled.
   * @throws AdminException if the creation fails.
   */
  public CommunityMembersGroup setUpCommunity(String spaceId, String symbol) throws AdminException {
    var currentUser = User.getCurrentRequester();
    var space = getCommunity(spaceId);
    var group = new CommunityMembersGroup(symbol + " " + space.getName(), spaceId);
    String groupId = admin.addGroup(group, true);
    var profile = getSpaceProfile(currentUser, SilverpeasRole.READER, space);
    profile.addGroup(groupId);
    admin.updateSpaceProfileInst(profile, currentUser.getId());
    return asAppManagedGroupDetail(admin.getGroup(groupId));
  }

  /**
   * Deletes the community in Silverpeas related by the specified members group. The community
   * space related by the group is identified with the {@link CommunityMembersGroup#getSpaceId()}
   * property. The Silverpeas space that backed the community will be then a regular
   * collaborative space the and the group of the community members will be deleted.
   *
   * @param spaceId the unique identifier of a community space
   * @param groupId the unique identifier of the group with the members of the community.
   * @throws AdminException if the community attached to the underlying Silverpeas space cannot
   * be deleted.
   */
  public void deleteCommunity(String spaceId, String groupId) throws AdminException {
    if (spaceId != null) {
      var space = admin.getSpaceInstById(spaceId);
      if (space != null) {
        space.setCommunitySpace(false);
        admin.updateSpaceInst(space);
      }
    }

    if (groupId != null) {
      Group group = admin.getGroup(groupId);
      if (group != null && group.isCommunityGroup()) {
        admin.deleteGroupById(group.getId(), true);
      }
    }
  }

  private void removeUserFromAllSpaceProfiles(User user, SpaceInst space) {
    User currentUser = User.getCurrentRequester();
    streamOnNonInheritedSpaceProfiles(space)
        .filter(p -> isUserHasProfile(user, p))
        .forEach(p -> {
          try {
            p.removeUser(user.getId());
            admin.updateSpaceProfileInst(p, currentUser.getId());
          } catch (Exception e) {
            throw new SilverpeasRuntimeException(e);
          }
        });
  }

  private Stream<SpaceProfileInst> streamOnNonInheritedSpaceProfiles(SpaceInst space) {
    return space.getAllSpaceProfilesInst().stream()
        .filter(not(SpaceProfileInst::isManager).and(not(SpaceProfileInst::isInherited)));
  }

  private CommunityMembersGroup asAppManagedGroupDetail(GroupDetail groupDetail) {
    return groupDetail == null ? null : new CommunityMembersGroup(groupDetail);
  }

  private SpaceProfileInst getSpaceProfile(User invoker, SilverpeasRole role, SpaceInst space)
      throws AdminException {

    var profile = space.getDirectSpaceProfileInst(role.getName());
    if (profile == null) {
      profile = new SpaceProfileInst();
      profile.setName(role.getName());
      profile.setSpaceFatherId(space.getId());
      profile.setInherited(false);
      admin.addSpaceProfileInst(profile, invoker.getId());
      space.addSpaceProfileInst(profile);
    }
    return profile;
  }

  private boolean isUserHasProfile(User user, SpaceProfileInst profile) {
    return profile.getAllUsers().contains(user.getId());
  }

  private boolean isUserInGroup(User user, GroupDetail group) {
    return List.of(group.getUserIds()).contains(user.getId());
  }
}
  