/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.USER;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.WRITER;
import static org.silverpeas.core.security.authorization.AccessControlOperation.isPersistActionFrom;
import static org.silverpeas.core.security.authorization.AccessControlOperation.isSharingActionFrom;

/**
 * Check the access to a node for a user.
 * @author ehugonnet
 */
@Service
@Singleton
public class NodeAccessController extends AbstractAccessController<NodePK>
    implements NodeAccessControl {

  private static final String DATA_MANAGER_CONTEXT_KEY = "NodeAccessControllerDataManager";

  private ComponentAccessControl componentAccessController;

  @Inject
  NodeAccessController(final ComponentAccessControl componentAccessController) {
    // Instance by IoC only.
    this.componentAccessController = componentAccessController;
  }

  static DataManager getDataManager(final AccessControlContext context) {
    DataManager manager = context.get(DATA_MANAGER_CONTEXT_KEY, DataManager.class);
    if (manager == null) {
      manager = new DataManager(context);
      context.put(DATA_MANAGER_CONTEXT_KEY, manager);
    }
    return manager;
  }

  @Override
  public Stream<NodePK> filterAuthorizedByUser(final Collection<NodePK> nodePks,
      final String userId, final AccessControlContext context) {
    final List<String> instancesIds =
        nodePks.stream().map(NodePK::getInstanceId).distinct().collect(Collectors.toList());
    getDataManager(context).loadCaches(userId, instancesIds);
    return nodePks.stream().filter(n -> isUserAuthorized(userId, n, context));
  }

  @Override
  public boolean isUserAuthorized(final String userId, final ResourceIdentifier id) {
    ContributionIdentifier nodeId = (ContributionIdentifier) id;
    NodePK nodePK = new NodePK(nodeId.getLocalId(), nodeId.getComponentInstanceId());
    NodeDetail nodeDetail = NodeService.get().getDetail(nodePK);
    return isUserAuthorized(userId, nodeDetail);
  }

  @Override
  public boolean isUserAuthorized(String userId, NodeDetail nodeDetail) {
    return isUserAuthorized(userId, nodeDetail, AccessControlContext.init());
  }

  @Override
  public boolean isUserAuthorized(String userId, NodeDetail nodeDetail,
      final AccessControlContext context) {
    getDataManager(context).loadCachesWithLoadedNode(nodeDetail);
    return isUserAuthorized(userId, nodeDetail.getNodePK(), context);
  }

  @Override
  public boolean isUserAuthorized(String userId, NodePK nodePK,
      final AccessControlContext context) {
    final Set<SilverpeasRole> userRoles = getUserRoles(userId, nodePK, context);
    final MemoizedSupplier<SilverpeasRole> highestRole = new MemoizedSupplier<>(() -> {
      final SilverpeasRole highestUserRole = SilverpeasRole.getHighestFrom(userRoles);
      return highestUserRole != null ? highestUserRole : SilverpeasRole.READER;
    });
    final ComponentAccessController.DataManager componentDataManager =
        ComponentAccessController.getDataManager(context);
    boolean authorized = isUserAuthorized(userRoles);
    if (authorized && componentDataManager.isTopicTrackerSupported(nodePK.getInstanceId()) &&
        nodePK.isTrash()) {
      authorized = highestRole.get().isGreaterThanOrEquals(WRITER);
    }
    if (authorized && isSharingActionFrom(context.getOperations())) {
      final SilverpeasRole highestUserRole = highestRole.get();
      final User user = User.getById(userId);
      authorized = !user.isAnonymous() && !user.isAccessGuest() &&
          componentDataManager.isFolderSharingEnabledForRole(nodePK.getInstanceId(),
              highestUserRole);
    }
    return authorized;
  }

  @Override
  public boolean isGroupAuthorized(final String groupId, final NodePK nodePK) {
    boolean authorized = false;
    if (componentAccessController.isGroupAuthorized(groupId, nodePK.getInstanceId())) {
      if (!componentAccessController.isRightOnTopicsEnabled(nodePK.getInstanceId())) {
        authorized = true;
      } else {
        try {
          final NodeDetail node = NodeService.get().getHeader(nodePK, false);
          if (node != null) {
            if (node.haveRights()) {
              NodePK objectPK = node.getNodePK();
              authorized = OrganizationController.get()
                  .isObjectAvailableToGroup(ProfiledObjectId.fromNode(node.getRightsDependsOn()),
                      objectPK.getInstanceId(), groupId);
            } else {
              authorized = true;
            }
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
          authorized = false;
        }
      }
    }
    return authorized;
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, NodePK nodePK) {
    final ComponentAccessController.DataManager componentDataManager =
        ComponentAccessController.getDataManager(context);
    final String instanceId = nodePK.getInstanceId();

    // Component access control
    final Set<SilverpeasRole> componentUserRoles =
        componentAccessController.getUserRoles(userId, instanceId, context);
    final boolean componentUserAuthorized = componentAccessController.isUserAuthorized(componentUserRoles);
    if (!componentUserAuthorized) {
      if (!nodePK.isRoot() && !nodePK.isTrash() && !nodePK.isUnclassed() &&
          componentDataManager.hasUserHasReadAccessAtLeast(instanceId) &&
          componentDataManager.isRightOnTopicsEnabled(instanceId)) {
        fillNodeUserRoles(userRoles, context, userId, nodePK, componentUserRoles);
      }
      return;
    }

    // If rights are not handled from the node, then filling the user role containers with these
    // of component
    if (!componentDataManager.isRightOnTopicsEnabled(instanceId) || nodePK.isRoot() ||
        nodePK.isTrash() || nodePK.isUnclassed()) {
      userRoles.addAll(componentUserRoles);
      return;
    }

    fillNodeUserRoles(userRoles, context, userId, nodePK, componentUserRoles);
  }

  private void fillNodeUserRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final NodePK nodePK,
      final Set<SilverpeasRole> componentUserRoles) {
    final DataManager dataManager = getDataManager(context);
    final NodeDetail node = dataManager.getNodeHeader(nodePK);
    if (node != null) {
      if (!node.haveRights()) {
        userRoles.addAll(componentUserRoles);
        return;
      }
      final Set<SilverpeasRole> nodeRoles = SilverpeasRole.fromStrings(dataManager.getUserProfiles(userId, node));
      if (isPersistActionFrom(context.getOperations())) {
        nodeRoles.remove(USER);
      }
      userRoles.addAll(nodeRoles);
    }
  }

  /**
   * Data manager.
   */
  static class DataManager {

    private final AccessControlContext context;
    private OrganizationController controller;
    private NodeService nodeService;
    Map<String, NodeDetail> nodeDetailCache = null;
    Map<Pair<String, String>, Set<String>> userProfiles = null;

    DataManager(final AccessControlContext context) {
      this.context = context;
      controller = OrganizationController.get();
      nodeService = NodeService.get();
    }

    void loadCachesWithLoadedNode(final NodeDetail nodeDetail) {
      nodeDetailCache = singletonMap(computeNodeCacheKey(nodeDetail.getNodePK()), nodeDetail);
    }

    void loadCaches(final String userId, final Collection<String> instanceIds) {
      if (userProfiles == null && !instanceIds.isEmpty()) {
        completeCaches(userId, instanceIds);
      }
    }

    void completeCaches(final String userId, final Collection<String> instanceIds) {
      final ComponentAccessController.DataManager componentDataManager =
          ComponentAccessController.getDataManager(context);
      final boolean firstLoad = nodeDetailCache == null;
      if (firstLoad) {
        componentDataManager.loadCaches(userId, instanceIds);
      } else {
        componentDataManager.completeCaches(userId, instanceIds);
      }
      final Set<String> instanceIdsWithRightsOnTopic = instanceIds.stream()
          .filter(componentDataManager::isRightOnTopicsEnabled)
          .collect(Collectors.toSet());
      if (firstLoad) {
        if (instanceIdsWithRightsOnTopic.isEmpty()) {
          nodeDetailCache = new HashMap<>(0);
          userProfiles = new HashMap<>(0);
        } else {
          final Pair<Map<String, NodeDetail>, Map<Pair<String, String>, Set<String>>> caches =
              loadNodesAndUserProfiles(userId, instanceIdsWithRightsOnTopic);
          nodeDetailCache = caches.getFirst();
          userProfiles = caches.getSecond();
        }
      } else {
        final Pair<Map<String, NodeDetail>, Map<Pair<String, String>, Set<String>>> caches =
            loadNodesAndUserProfiles(userId, instanceIdsWithRightsOnTopic);
        caches.getFirst().forEach((k, v) -> nodeDetailCache.put(k, v));
        caches.getSecond().forEach((k, v) -> userProfiles.put(k, v));
      }
    }

    private Pair<Map<String, NodeDetail>, Map<Pair<String, String>, Set<String>>> loadNodesAndUserProfiles(
        final String userId, final Set<String> instanceIdsWithRightsOnTopic) {
      final List<NodeDetail> nodeDetails =
          nodeService.getMinimalDataByInstances(instanceIdsWithRightsOnTopic);
      final Map<String, NodeDetail> currentNodeDetailCache = new HashMap<>(nodeDetails.size());
      nodeDetails.forEach(n -> currentNodeDetailCache.put(computeNodeCacheKey(n.getNodePK()), n));
      final Set<String> nodeIds = nodeDetails.stream()
          .map(NodeDetail::getRightsDependsOn)
          .filter(i -> !i.equals(NodeDetail.NO_RIGHTS_DEPENDENCY))
          .collect(Collectors.toSet());
      final Map<Pair<String, String>, Set<String>> currentUserProfiles;
      if (nodeIds.isEmpty()) {
        currentUserProfiles = new HashMap<>(0);
      } else {
        currentUserProfiles =
            controller.getUserProfilesByComponentIdAndObjectId(userId, instanceIdsWithRightsOnTopic,
                ProfiledObjectIds.fromNodeIds(nodeIds));
      }
      return Pair.of(currentNodeDetailCache, currentUserProfiles);
    }

    private String computeNodeCacheKey(final NodePK nodePK) {
      return nodePK.getInstanceId() + "@" + nodePK.getId();
    }

    String[] getUserProfiles(final String userId, final NodeDetail node) {
      final NodePK nodePK = node.getNodePK();
      if (userProfiles != null) {
        final Pair<String, String> key = Pair.of(nodePK.getInstanceId(), node.getRightsDependsOn());
        return userProfiles.getOrDefault(key, emptySet()).toArray(new String[0]);
      }
      return controller.getUserProfiles(userId, nodePK.getInstanceId(),
          ProfiledObjectId.fromNode(node.getRightsDependsOn()));
    }

    public NodeDetail getNodeHeader(final NodePK nodePK) {
      if (nodeDetailCache != null) {
        return nodeDetailCache.get(computeNodeCacheKey(nodePK));
      }
      try {
        return nodeService.getHeader(nodePK, false);
      } catch (NodeRuntimeException ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
        return null;
      }
    }
  }
}
