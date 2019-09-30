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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.MemoizedBooleanSupplier;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider.getSessionVolatileResourceCacheService;
import static org.silverpeas.core.security.authorization.AccessControlOperation.isPersistActionFrom;
import static org.silverpeas.core.security.authorization.AccessControlOperation.isSharingActionFrom;

/**
 * Check the access to a publication for a user.
 * @author neysseric
 */
@Singleton
public class PublicationAccessController extends AbstractAccessController<PublicationPK>
    implements PublicationAccessControl {

  static final String PUBLICATION_DETAIL_KEY = "PUBLICATION_DETAIL_KEY";

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private NodeAccessControl nodeAccessController;

  @Inject
  private PublicationService publicationService;

  PublicationAccessController() {
    // Instance by IoC only.
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationPK pubPk,
      final AccessControlContext context) {
    return isUserAuthorizedByContext(userId, pubPk, context, getUserRoles(userId, pubPk, context));
  }

  /**
   * @param userId
   * @param pubPk
   * @param context
   * @param userRoles
   * @return
   */
  private boolean isUserAuthorizedByContext(String userId, PublicationPK pubPk,
      final AccessControlContext context, Set<SilverpeasRole> userRoles) {
    if (userRoles.isEmpty()) {
      // For now there is no point to perform the next verifications
      return false;
    }
    final Set<AccessControlOperation> operations = context.getOperations();
    final Mutable<Boolean> authorized = Mutable.of(true);
    final SilverpeasRole safeHighestUserRole = getSafeSilverpeasRole(userRoles);
    final String instanceId = pubPk.getInstanceId();
    final PublicationDetail publicationDetail = context.get(PUBLICATION_DETAIL_KEY, PublicationDetail.class);
    final BooleanSupplier canPublicationBePersistedOrDeleted = new MemoizedBooleanSupplier(
        () -> getComponentExtension(instanceId).canPublicationBePersistedOrDeletedBy(publicationDetail, instanceId, userId, safeHighestUserRole));

    // Verifying simple access
    if (publicationDetail != null && !canPublicationBePersistedOrDeleted.getAsBoolean()) {
      authorized.set(publicationDetail.isValid() && publicationDetail.isVisible());
    }

    // Verifying sharing
    authorized.filter(a -> a && isSharingActionFrom(operations))
              .ifPresent(a -> {
      User user = User.getById(userId);
      authorized.set(!user.isAnonymous() && componentAccessController
          .isPublicationSharingEnabledForRole(instanceId, safeHighestUserRole));
    });

    // Verifying persist actions
    authorized.filter(a -> a && isPersistActionFrom(operations))
              .ifPresent(a -> authorized.set(canPublicationBePersistedOrDeleted.getAsBoolean()));
    return authorized.get();
  }

  ComponentInstancePublicationAccessControlExtension getComponentExtension(final String instanceId) {
    return ComponentInstancePublicationAccessControlExtension.getByInstanceId(instanceId);
  }

  private SilverpeasRole getSafeSilverpeasRole(final Set<SilverpeasRole> userRoles) {
    SilverpeasRole safeHighestUserRole = SilverpeasRole.getHighestFrom(userRoles);
    if (safeHighestUserRole == null) {
      // Preventing from technical errors by using a role which is giving no authorization
      safeHighestUserRole = SilverpeasRole.reader;
    }
    return safeHighestUserRole;
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, PublicationPK publicationPK) {
    final String instanceId = publicationPK.getInstanceId();
    final String pubId = publicationPK.getId();

    // Component access control
    final Set<SilverpeasRole> componentUserRoles = componentAccessController.getUserRoles(userId, instanceId, context);
    final boolean componentAccessAuthorized = componentAccessController.isUserAuthorized(componentUserRoles);

    final boolean isNotCreationContext = StringUtil.isInteger(pubId) &&
        !getSessionVolatileResourceCacheService().contains(pubId, instanceId);
    if (isNotCreationContext) {
      final PublicationDetail pubDetail;
      try {
        pubDetail = getActualForeignPublication(pubId, instanceId);
        context.put(PUBLICATION_DETAIL_KEY, pubDetail);
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
        return;
      }
      if (componentAccessController.isTopicTrackerSupported(instanceId)
          && fillTopicTrackerRoles(userRoles, context, userId, componentAccessAuthorized, pubDetail)) {
        return;
      }
    }
    if (componentAccessAuthorized) {
      userRoles.addAll(componentUserRoles);
    }
  }

  /**
   * Fills given userRoles with the given context.
   * @param userRoles the {@link Set} to fill.
   * @param context the context.
   * @param userId the identifier of the current user.
   * @param componentAccessAuthorized is component access authorized.
   * @param pubDetail the publication data.
   * @return true if roles have been handled, false otherwise.
   */
  private boolean fillTopicTrackerRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId,
      final boolean componentAccessAuthorized, final PublicationDetail pubDetail) {
    boolean rolesProcessed = false;
    if (!componentAccessAuthorized) {
      // Check if an alias of publication is authorized
      // (special treatment in case of the user has no access right on component instance)
      rolesProcessed = fillTopicTrackerAliasRoles(userRoles, context, userId, pubDetail);
    } else if (componentAccessController.isRightOnTopicsEnabled(pubDetail.getInstanceId())) {
      // If rights are handled on folders, folder rights are checked !
      rolesProcessed = fillTopicTrackerNodeRoles(userRoles, context, userId, pubDetail);
      if (rolesProcessed && CollectionUtil.isEmpty(userRoles)) {
        // if the publication is not on root node and if user has no rights on folder, check if
        // an alias of publication is authorized
        fillTopicTrackerAliasRoles(userRoles, context, userId, pubDetail);
      }
    }
    return rolesProcessed;
  }

  private boolean fillTopicTrackerNodeRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final PublicationDetail pubDetail) {
    try {
      final Optional<Location> mainLocation = getPublicationService().getMainLocation(pubDetail.getPK());
      if (mainLocation.isPresent()) {
        final Set<SilverpeasRole> nodeUserRoles = nodeAccessController
            .getUserRoles(userId, mainLocation.get(), context);
        if (nodeAccessController.isUserAuthorized(nodeUserRoles)) {
          userRoles.addAll(nodeUserRoles);
        }
      } else {
        // case of publications on root node (so component rights will be checked)
        return false;
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
    return true;
  }

  private boolean fillTopicTrackerAliasRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final PublicationDetail pubDetail) {
    try {
      final Collection<Location> locations = getPublicationService().getAllAliases(pubDetail.getPK());
      for (final Location location : locations) {
        final Set<SilverpeasRole> nodeUserRoles = nodeAccessController.getUserRoles(userId, location, context);
        if (nodeAccessController.isUserAuthorized(nodeUserRoles)) {
          userRoles.addAll(nodeUserRoles);
          break;
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return true;
  }

  protected PublicationService getPublicationService() {
    return publicationService;
  }

  /**
   * Return the 'real' publication to which this file is attached to. In case of a clone
   * publication we need the cloned one (that is the original publication).
   * @param foreignId
   * @param instanceId
   * @return
   */
  private PublicationDetail getActualForeignPublication(String foreignId, String instanceId) {
    PublicationDetail pubDetail =
        getPublicationService().getDetail(new PublicationPK(foreignId, instanceId));
    if (!pubDetail.isValid() && pubDetail.haveGotClone()) {
      pubDetail =
          getPublicationService().getDetail(new PublicationPK(pubDetail.getCloneId(), instanceId));
    }
    return pubDetail;
  }
}