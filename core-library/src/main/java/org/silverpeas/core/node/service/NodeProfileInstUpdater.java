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

package org.silverpeas.core.node.service;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An updater of all the right profiles of the nodes managed in a given component instance. This is
 * a service dedicated to be used by the listeners of events about change in the roles played by
 * some users or by some user groups in order to to synchronize those change to the right profiles
 * of the nodes having specific local right accesses and belonging to the component instance.
 *
 * @author mmoquillon
 */
@Service
public class NodeProfileInstUpdater {

  @Inject
  private Administration admin;
  @Inject
  private NodeService nodeService;

  /**
   * Gets a remover of both users and groups from right profiles of nodes belonging to the specified
   * component instance.
   *
   * @param componentInstanceId the unique identifier of a component instance.
   * @return a remover instance.
   */
  public Remover getRemoverFor(String componentInstanceId) {
    return new Remover(componentInstanceId);
  }

  /**
   * A remover of users and user groups from the roles specific to the nodes having local access
   * rights. The users and the groups are removed only if they don't play at least one role in the
   * component instance.
   */
  public class Remover {

    private final String instanceId;
    private final Set<String> userIds = new HashSet<>();
    private final Set<String> groupIds = new HashSet<>();

    /**
     * Constructs a new remover object for the specified component instance.
     *
     * @param instanceId the unique identifier of a component instance
     */
    Remover(String instanceId) {
      this.instanceId = instanceId;
    }

    /**
     * Sets the users to be removed from the right profiles of the nodes.
     *
     * @param userIds a set with the unique identifier of the users to remove.
     * @return itself.
     */
    Remover ofUsers(Set<String> userIds) {
      this.userIds.clear();
      this.userIds.addAll(userIds);
      return this;
    }

    /**
     * Sets the user groups to be removed from the right profiles of the nodes.
     *
     * @param groupIds a set with the unique identifier of the user groups to remove.
     * @return itself.
     */
    Remover ofGroups(Set<String> groupIds) {
      this.groupIds.clear();
      this.groupIds.addAll(groupIds);
      return this;
    }

    /**
     * Applies the remove. All the users and user groups sets will be removed from the right
     * profiles of each nodes of the component instance having a specific local access rights if and
     * only if they don't play any other role in the component instance.
     */
    public void apply() {
      nodeService.getDescendantDetails(new NodePK(NodePK.ROOT_NODE_ID, instanceId)).stream()
          .filter(NodeDetail::haveLocalRights)
          .forEach(node ->
              getNodeRoles(node)
                  .forEach(role -> {
                    groupIds.stream()
                        .filter(group -> role.getAllGroups().contains(group))
                        .filter(group -> isGroupNotPlayedAnyRole(group, instanceId))
                        .forEach(group -> removeGroupFromRole(group, role));
                    userIds.stream()
                        .filter(user -> role.getAllUsers().contains(user))
                        .filter(user -> isUserNotPlayingAnotherRole(user, instanceId))
                        .forEach(user -> removeUserFromRole(user, role));
                  }));
    }

    private void removeUserFromRole(String userId, ProfileInst role) {
      try {
        role.removeUser(userId);
        admin.updateProfileInst(role);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    private void removeGroupFromRole(String groupId, ProfileInst role) {
      try {
        role.removeGroup(groupId);
        admin.updateProfileInst(role);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    private List<ProfileInst> getNodeRoles(NodeDetail node) {
      try {
        return admin.getProfilesByObject(ProfiledObjectId.fromNode(node.getId()),
            node.getIdentifier().getComponentInstanceId());
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    private ProfileInst getProfileInst(String profileId) {
      try {
        return admin.getProfileInst(profileId);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    private boolean isUserNotPlayingAnotherRole(String userId, String instanceId) {
      try {
        String[] roleNames = admin.getCurrentProfiles(userId, instanceId);
        return roleNames.length == 0;
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    private boolean isGroupNotPlayedAnyRole(String groupId, String instanceId) {
      try {
        int localId = ComponentInst.getComponentLocalId(instanceId);
        return Stream.of(admin.getProfileIdsOfGroup(groupId))
            .map(this::getProfileInst)
            .filter(ProfileInst::isOnComponentInstance)
            .noneMatch(p -> p.getComponentFatherId() == localId);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}
  