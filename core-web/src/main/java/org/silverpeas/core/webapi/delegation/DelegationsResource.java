/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.webapi.delegation;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.delegation.Delegation;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.webapi.delegation.DelegationEntity.asWebEntity;

/**
 * It represents the Web resource one or more delegations of roles between users in Silverpeas.
 * @author mmoquillon
 */
@Service
@RequestScoped
@Authenticated
@Path(DelegationsResource.DELEGATION_BASE_URI)
public class DelegationsResource extends RESTWebService {

  static final String DELEGATION_BASE_URI = "delegations";

  /**
   * Gets all the delegations of roles that were done by the given user.
   * @return a list of all delegations of user roles. If no delegations exist, then an empty list
   * is returned.
   */
  @Path("delegators/{userId}")
  @GET
  public List<DelegationEntity> getAllDelegationsByDelegator(
      @PathParam("userId") final String userId) {
    return Delegation.getAllFrom(User.getById(userId))
        .stream()
        .map(d -> asWebEntity(d, identifiedBy(d.getId())))
        .collect(Collectors.toList());
  }

  /**
   * Gets all the delegations of roles that were done to the given user.
   * @return a list of all delegations of user roles. If no delegations exist, then an empty list
   * is returned.
   */
  @Path("delegates/{userId}")
  @GET
  public List<DelegationEntity> getAllDelegationsToDelegates(
      @PathParam("userId") final String userId) {
    return Delegation.getAllTo(User.getById(userId))
        .stream()
        .map(d -> asWebEntity(d, identifiedBy(d.getId())))
        .collect(Collectors.toList());
  }

  /**
   * Gets all the delegations of roles that were done by the given user for the specified
   * component instance.
   * @return a list of all delegations of user roles. If no delegations exist, then an empty list
   * is returned.
   */
  @Path("delegators/{userId}/{componentId}")
  @GET
  public List<DelegationEntity> getAllDelegationsByDelegates(
      @PathParam("userId") final String userId,
      @PathParam("componentId") final String componentId) {
    return Delegation.getAllFrom(User.getById(userId), componentId)
        .stream()
        .map(d -> asWebEntity(d, identifiedBy(d.getId())))
        .collect(Collectors.toList());
  }

  /**
   * Gets all the delegations of roles that were done to the given user and for the specified
   * component instance.
   * @return a list of all delegations of user roles. If no delegations exist, then an empty list
   * is returned.
   */
  @Path("delegates/{userId}/{componentId}")
  @GET
  public List<DelegationEntity> getAllDelegationsToDelegates(
      @PathParam("userId") final String userId,
      @PathParam("componentId") final String componentId) {
    return Delegation.getAllTo(User.getById(userId), componentId)
        .stream()
        .map(d -> asWebEntity(d, identifiedBy(d.getId())))
        .collect(Collectors.toList());
  }

  @Override
  protected String getResourceBasePath() {
    return DELEGATION_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return null;
  }

}
  