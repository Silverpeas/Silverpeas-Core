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

package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.route.AdminComponentRequestRouter;

import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This abstraction centralizes common processes dedicated to several
 * {@link AbstractComponentSessionController} implementations in charge of the management of
 * administration services.
 * <p>
 *   To be fully functional, an implementation of this abstraction MUST be used with an
 *   {@link AdminComponentRequestRouter} implementation.
 * </p>
 * @author silveryocha
 */
public abstract class AbstractAdminComponentSessionController
    extends AbstractComponentSessionController {
  private static final long serialVersionUID = 866731879572404115L;

  public AbstractAdminComponentSessionController(final MainSessionController controller,
      final ComponentContext context, final String localizedMessagesBundleName) {
    super(controller, context, localizedMessagesBundleName);
  }

  public AbstractAdminComponentSessionController(final MainSessionController controller,
      final ComponentContext context, final String localizedMessagesBundleName,
      final String iconFileName) {
    super(controller, context, localizedMessagesBundleName, iconFileName);
  }

  public AbstractAdminComponentSessionController(final MainSessionController controller,
      final ComponentContext context, final String localizedMessagesBundleName,
      final String iconFileName, final String settingsFileName) {
    super(controller, context, localizedMessagesBundleName, iconFileName, settingsFileName);
  }

  @Override
  public void setAppModeMaintenance(final boolean mode) {
    checkAdminAccessOnly();
    super.setAppModeMaintenance(mode);
  }

  @Override
  public void setSpaceModeMaintenance(final String spaceId, final boolean mode) {
    if (isDefined(spaceId)) {
      checkAccessGranted(spaceId, null, false);
    } else {
      throwForbiddenError();
    }
    super.setSpaceModeMaintenance(spaceId, mode);
  }

  public boolean isUserAdmin() {
    return getUserDetail().isAccessAdmin();
  }

  /**
   * Checks the user has full admin access.
   */
  public void checkAdminAccessOnly() {
    if (!isUserAdmin()) {
      throwForbiddenError();
    }
  }

  /**
   * Used mainly by {@link #checkAccessGranted()}.
   * <p>
   *   Each implementation can precise or change this default implementation.
   * </p>
   * @return true if access granted, false otherwise.
   */
  public boolean isAccessGranted() {
    return isUserAdmin();
  }

  /**
   * This method is invoked at each administration service access.
   * @throws ForbiddenRuntimeException in case the user has forbidden access.
   */
  public void checkAccessGranted() {
    if (!isAccessGranted()) {
      throwForbiddenError();
    }
  }

  /**
   * Indicates if the user can access administration about a space or a component instance.
   * <p>
   *   Whatever the parameters, user having admin access right on its account is always access
   *   granted.
   * </p>
   * <p>
   *   If both space id and instance id are given, they are both verified.
   * </p>
   * <p>
   *   If neither space if neither instance id are given, it is verified that current user is a
   *   space manager.
   * </p>
   * @param spaceId the optional identifier of a space.
   * @param instanceId the optional identifier of a component instance.
   * @param readOnly true if the operation is read only, false otherwise.
   * @return true if access granted, false otherwise.
   */
  public boolean isAccessGranted(final String spaceId, final String instanceId,
      final boolean readOnly)
      throws ForbiddenRuntimeException {
    boolean accessGranted = getUserDetail().isAccessAdmin();
    if (!accessGranted) {
      final String[] userManageableSpaceIds = getUserManageableSpaceIds();
      accessGranted = userManageableSpaceIds.length > 0 &&
          (readOnly || isDefined(spaceId) || isDefined(instanceId));
      if (accessGranted) {
        final Set<String> userManageableSpaceIdSet = of(userManageableSpaceIds).collect(toSet());
        if (isDefined(instanceId)) {
          accessGranted = getOrganisationController().getComponentInstance(instanceId)
              .map(SilverpeasComponentInstance::getSpaceId)
              .map(userManageableSpaceIdSet::contains)
              .orElse(false);
        }
        if (accessGranted && isDefined(spaceId)) {
          accessGranted = ofNullable(getOrganisationController().getSpaceInstById(spaceId))
              .map(SpaceInst::getId)
              .map(s -> userManageableSpaceIdSet.contains(s) ||
                  (readOnly && of(userManageableSpaceIds)
                      .map(getOrganisationController()::getSpaceInstById)
                      .filter(Objects::nonNull)
                      .map(SpaceInst::getDomainFatherId)
                      .filter(StringUtil::isDefined)
                      .filter(not(userManageableSpaceIdSet::contains))
                      .flatMap(m -> getOrganisationController().getPathToSpace(m).stream())
                      .map(SpaceInstLight::getId)
                      .anyMatch(s::equals)))
              .orElse(false);
        }
      }
    }
    return accessGranted;
  }

  /**
   * Checks the user can access administration about a space or a component instance.
   * <p>
   *   Whatever the parameters, user having admin access right on its account is always access
   *   granted.
   * </p>
   * <p>
   *   If both space id and instance id are given, they are both verified.
   * </p>
   * <p>
   *   If neither space if neither instance id are given, it is verified that current user is a
   *   space manager.
   * </p>
   * @param spaceId the optional identifier of a space.
   * @param instanceId the optional identifier of a component instance.
   * @param readOnly true if the operation is read only, false otherwise.
   * @throws ForbiddenRuntimeException in case the user has forbidden access.
   */
  public void checkAccessGranted(final String spaceId, final String instanceId,
      final boolean readOnly)
      throws ForbiddenRuntimeException {
    if (!isAccessGranted(spaceId, instanceId, readOnly)) {
      throwForbiddenError();
    }
  }

  protected void throwForbiddenError() {
    throw new ForbiddenRuntimeException(
        String.format("Forbidden admin access to user with id %s", getUserId()));
  }
}
