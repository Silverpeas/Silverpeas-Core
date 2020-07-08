/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.webapi.admin.scim;

import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.UpdateRequest;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimUser;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.webapi.profile.UserProfilesSearchCriteriaBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;
import static org.silverpeas.core.admin.user.constant.UserAccessLevel.USER;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;
import static org.silverpeas.core.webapi.admin.scim.SilverpeasScimServerConverter.*;

/**
 * <p>
 * The service in charge of handling the Silverpeas's users against those sent by SCIM client.<br/>
 * It is finally kind of CRUD service.
 * </p>
 * <p>
 * All provider methods are called by WEB services which are decoding the HTTP requests in front.
 * </p>
 * @author silveryocha
 */
@Service
public class ScimUserAdminService extends AbstractScimAdminService implements Provider<ScimUser> {

  @Override
  public ScimUser create(final ScimUser resource) throws UnableToCreateResourceException {
    logger().debug(() -> "creating user " + resource);
    validateDomainExists();
    try {
      final UserFull user = convert(resource);
      user.setDomainId(scimRequestContext.getDomainId());
      if (isNotDefined(user.getLastName())) {
        logger().debug(() -> "using 'UserName' SCIM attribute for user last name");
        user.setLastName(resource.getUserName());
      }
      user.setAccessLevel(USER);
      final String newSpUserId = admin.addUser(user);
      user.setId(newSpUserId);
      return convert(user);
    } catch (Exception e) {
      Domain domain;
      try {
        domain = Administration.get().getDomain(scimRequestContext.getDomainId());
      } catch (AdminException ae) {
        throw new UnableToCreateResourceException(FORBIDDEN, ae.getMessage());
      }
      throw new UnableToCreateResourceException(domain.isQuotaReached() ? FORBIDDEN : NOT_FOUND,
          e.getMessage());
    }
  }

  @Override
  public ScimUser update(final UpdateRequest<ScimUser> updateRequest)
      throws UnableToUpdateResourceException {
    ScimUser resource;
    try {
      resource = updateRequest.getResource();
    } catch (final UnsupportedOperationException e) {
      resource = null;
    }
    if (resource != null) {
      logger().debug(() -> "updating user " + updateRequest.getOriginal());
      validateDomainExists();
      try {
        final UserFull user = getUserById(updateRequest.getId());
        applyTo(resource, user);
        return update(user);
      } catch (Exception e) {
        throw new UnableToUpdateResourceException(NOT_FOUND, e.getMessage());
      }
    } else {
      logger().debug(() -> "patching user " + updateRequest.getOriginal());
      validateDomainExists();
      if (CollectionUtil.isEmpty(updateRequest.getPatchOperations())) {
        throw new UnableToUpdateResourceException(BAD_REQUEST, "no attribute to patch");
      }
      try {
        final ScimUser scimUser = updateRequest.getOriginal();
        final PatchOperationApplier operationApplier = new PatchOperationApplier(scimUser);
        updateRequest.getPatchOperations().forEach(operationApplier::apply);
        final UserFull user = getUserById(updateRequest.getId());
        applyTo(scimUser, user);
        return update(user);
      } catch (Exception e) {
        throw new UnableToUpdateResourceException(NOT_FOUND, e.getMessage());
      }
    }
  }

  private ScimUser update(UserFull updatedUser) throws UnableToUpdateResourceException {
    if (isNotDefined(updatedUser.getLastName())) {
      logger().debug(() -> "using 'UserName' SCIM attribute for user last name");
      updatedUser.setLastName(updatedUser.getLogin());
    }
    try {
      admin.updateUser(updatedUser);
      return convert(updatedUser);
    } catch (AdminException e) {
      throw new UnableToUpdateResourceException(NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public ScimUser get(final String id) throws UnableToRetrieveResourceException {
    logger().debug(() -> "getting user by id " + id);
    validateDomainExists();
    try {
      final UserFull user = getUserById(id);
      return convert(user);
    } catch (Exception e) {
      throw new UnableToRetrieveResourceException(NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public FilterResponse<ScimUser> find(final Filter filter, final PageRequest pageRequest,
      final SortRequest sortRequest) throws UnableToRetrieveResourceException {
    final String domainId = scimRequestContext.getDomainId();
    logger().debug(() -> "looking for users into domain " + domainId + " by filtering on " + filter);
    validateDomainExists();
    try {
      final UserProfilesSearchCriteriaBuilder criteriaBuilder =
          UserProfilesSearchCriteriaBuilder.aSearchCriteria().withDomainIds(domainId);

      final int startIndex = pageRequest.getStartIndex() != null ? pageRequest.getStartIndex() : 0;
      final int itemPerPage = pageRequest.getCount() != null ? pageRequest.getCount() : 10;
      if (startIndex > 0) {
        criteriaBuilder.withPaginationPage(
            new PaginationPage(((startIndex - 1) / itemPerPage) + 1, itemPerPage));
      }

      final SilverpeasList<UserDetail> users = admin
          .searchUsers(processExpression(filter.getExpression(), criteriaBuilder.build()));

      final FilterResponse<ScimUser> response = new FilterResponse<>();
      response.setResources(
          users.stream().map(SilverpeasScimServerConverter::convert).collect(Collectors.toList()));

      if (users.originalListSize() > 0) {
        final PageRequest paginationResult = new PageRequest();
        paginationResult.setStartIndex(startIndex > 0 ? startIndex : 1);
        paginationResult.setCount(startIndex > 0 ? itemPerPage : (int) users.originalListSize());
        response.setPageRequest(paginationResult);
      }

      response.setTotalResults((int) users.originalListSize());
      logger().debug(() -> "finding " + users.originalListSize() + " user(s)");
      return response;
    } catch (Exception e) {
      throw new UnableToRetrieveResourceException(NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public void delete(final String id) throws UnableToDeleteResourceException {
    logger().debug(() -> "deleting user by id " + id);
    validateDomainExists();
    try {
      final User user = getUserById(id);
      admin.deleteUser(user.getId());
    } catch (Exception e) {
      throw new UnableToDeleteResourceException(NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    logger().debug(() -> "getting user SCIM extensions");
    return Collections.singletonList(EnterpriseExtension.class);
  }

  private UserFull getUserById(final String id) throws AdminException {
    final UserFull user = admin.getUserFull(decodeUserId(id));
    if (!scimRequestContext.getDomainId().equals(user.getDomainId())) {
      throw new AdminException("trying to manage a user of a forbidden domain!");
    }
    return user;
  }
}
