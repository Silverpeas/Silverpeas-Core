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

package org.silverpeas.core.webapi.workflow;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.webapi.workflow.ReplacementEntity.asWebEntity;

/**
 * Web resource representing the replacements of a user by one or more other users for some of its
 * tasks in a given workflow. The goal of a user replacement is to allow another user, with the
 * requirement of having the same role, to complete a task in the incumbent's stead.
 * @author mmoquillon
 */
@Service
@RequestScoped
@Authorized
@Path("workflow/{workflowId}/replacements")
public class ReplacementResource extends RESTWebService {

  private static final MessageFormat REPLACEMENT_BASE_URI =
      new MessageFormat("workflow/{0}/replacements");

  @PathParam("workflowId")
  private String workflowId;

  @Inject
  private UserManager userManager;

  /**
   * Gets all the replacements of the specified user in the given underlying workflow.
   * Only the replacements into which the requester is concerned are returned unless he plays the
   * role of supervisor in the given underlying workflow.
   * @return a list of all the replacements of the given user. If no replacements exist, then an
   * empty list is returned.
   */
  @Path("incumbents/{userId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReplacementEntity> getAllReplacementsOfUser(
      @PathParam("userId") final String userId) throws WorkflowException {
    final User incumbent = getUser(userId);
    final Stream<Replacement> replacements = Replacement.getAllOf(incumbent, workflowId).stream();
    return filterAndEncode(replacements);
  }

  /**
   * Gets all the replacements that are being accomplished by the specified of user in the given
   * underlying workflow. Only the replacements into which the requester is concerned are
   * returned unless he plays the role of supervisor in the given underlying workflow.
   * @return a list of all the replacements accomplished by the given user. If no replacements
   * exist, then an empty list is returned.
   */
  @Path("substitutes/{userId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReplacementEntity> getAllReplacementsByUser(
      @PathParam("userId") final String userId) throws WorkflowException {
    final User substitute = getUser(userId);
    final Stream<Replacement> replacements = Replacement.getAllBy(substitute, workflowId).stream();
    return filterAndEncode(replacements);
  }

  /**
   * Gets all the replacements that were created in the underlying workflow. Only the workflow
   * supervisor can invoke this method.
   * @return a list of all replacements that are defined for the underlying workflow.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReplacementEntity> getAllReplacements() {
    return process(() -> {
      final Stream<Replacement> replacements = Replacement.getAll(workflowId).stream();
      return encode(replacements);
    }).lowestAccessRole(SilverpeasRole.supervisor).execute();
  }

  @Override
  protected String getResourceBasePath() {
    return REPLACEMENT_BASE_URI.format(new Object[]{this.workflowId});
  }

  @Override
  public String getComponentId() {
    return workflowId;
  }

  private User getUser(final String userId) throws WorkflowException {
    return userManager.getUser("me".equals(userId) ? getUser().getId() : userId);
  }

  private boolean isRequesterSupervisor() {
    boolean isSupervisor;
    try {
      isSupervisor =
          Stream.of(userManager.getUsersInRole(SilverpeasRole.supervisor.name(), workflowId))
          .anyMatch(u -> u.getUserId().equals(getUser().getId()));
    } catch (WorkflowException e) {
      isSupervisor = false;
    }
    return isSupervisor;
  }

  private Predicate<Replacement> onTheRequester =
      r -> r.getSubstitute().getUserId().equals(getUser().getId()) ||
          r.getIncumbent().getUserId().equals(getUser().getId());

  private List<ReplacementEntity> filterAndEncode(final Stream<Replacement> replacements) {
    Stream<Replacement> stream = replacements;
    if (!isRequesterSupervisor()) {
      stream = replacements.filter(onTheRequester);
    }
    return encode(stream);
  }

  private List<ReplacementEntity> encode(final Stream<Replacement> replacements) {
    return replacements.map(r -> asWebEntity(r, identifiedBy(r.getId())))
        .collect(Collectors.toList());
  }

}
  