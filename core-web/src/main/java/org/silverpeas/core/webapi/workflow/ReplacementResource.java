/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.workflow;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.ReplacementList;
import org.silverpeas.core.workflow.api.user.User;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.workflow.ReplacementEntity.asWebEntity;

/**
 * Web resource representing the replacements of a user by one or more other users for some of its
 * tasks in a given workflow. The goal of a user replacement is to allow another user, with the
 * requirement of having the same role, to complete a task in the incumbent's stead.
 * @author mmoquillon
 */
@WebService
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
   * Gets all the replacements that were created in the requested workflow. The replacements can
   * be filtered by one incumbent or by one substitute taking part in the replacements to
   * returns. If both are specified, then only the replacements in which both of them take part are
   * returned.
   * <p>
   * For security reason, when one incumbent or one substitute is specified, only the replacements
   * in which the requester is concerned (either as incumbent or as substitute) are returned.
   * Unless the requester is a supervisor in the requested workflow, if he is neither an
   * incumbent nor a substitute in any replacements, then nothing is returned.
   * </p>
   * <p>
   * Only the supervisor of the given requested workflow have the rights to asks for all of the
   * replacements in the workflow or to request any replacements in which a given user takes part
   * (either as an incumbent or as a substitute).
   * </p>
   * @return a list of all replacements that are defined in the requested workflow.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends Replacement<T>> Collection<ReplacementEntity> getAllReplacements(
      @QueryParam("incumbent") final String incumbentId,
      @QueryParam("substitute") final String substituteId) {
    final Stream<T> stream;
    if (isDefined(incumbentId) && isDefined(substituteId)) {
      final User incumbent = getUser(incumbentId);
      final User substitute = getUser(substituteId);
      final List<T> replacements =
          Replacement.getAllWith(incumbent, substitute, workflowId);
      stream = filterOnTheRequester(replacements.stream());
    } else if (isDefined(incumbentId)) {
      final User incumbent = getUser(incumbentId);
      final List<T> replacements = Replacement.getAllOf(incumbent, workflowId);
      stream = filterOnTheRequester(replacements.stream());
    } else if (isDefined(substituteId)) {
      final User substitute = getUser(substituteId);
      final List<T> replacements = Replacement.getAllBy(substitute, workflowId);
      stream = filterOnTheRequester(replacements.stream());
    } else {
      stream = process(() -> {
        ReplacementList<T> replacements = Replacement.getAll(workflowId);
        return replacements.stream();
      }).lowestAccessRole(SilverpeasRole.SUPERVISOR).execute();
    }
    return encode(stream);
  }

  /**
   * Gets the replacement with the specified unique identifier in the requested workflow.
   * If no such replacement exists in the given workflow, then an HTTP error
   * {@link Response.Status#NOT_FOUND} is sent back.
   * <p>
   * For security reason, unless the requester is a supervisor in the requested workflow, only
   * the users concerned by the asked replacement (either as incumbent or as substitute) can
   * ask for the targeted replacement.
   * </p>
   * @param replacementId the unique identifier of a replacement in the requested workflow.
   * @return ReplacementEntity representation of the replacement.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  public ReplacementEntity getReplacement(@PathParam("id") final String replacementId) {
    final Replacement<?> replacement = getReplacementById(replacementId);
    return asWebEntity(replacement, getUri().getAbsolutePath());
  }

  /**
   * Creates a new replacement in the given requested workflow from the specified entity embodied
   * in the incoming request. Be caution: the workflow identifier in the entity must match the
   * requested workflow instance otherwise an HTTP error {@link Response.Status#BAD_REQUEST} is
   * sent back.
   * <p>
   * For security reason, unless the requester plays the role of supervisor in the requested
   * workflow, he must be the incumbent of the tasks he asks for replacement. Otherwise an HTTP
   * error {@link Response.Status#FORBIDDEN} is sent back. Only the supervisor can create a
   * replacement between two others users in a workflow instance or for himself.
   * </p>
   * @param entity the entity providing the information about the replacement to create.
   * @return the response with the status {@link Response.Status#CREATED} and with the entity
   * representing the newly created replacement.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response createNewReplacement(final ReplacementEntity entity) {
    assertIsValid(entity);
    final User incumbent = getUser(entity.getIncumbent().getId());
    final User substitute = getUser(entity.getSubstitute().getId());
    Replacement<?> replacement = Replacement.between(incumbent, substitute)
        .inWorkflow(workflowId)
        .during(Period.between(entity.getStartDate(), entity.getEndDate()))
        .save();
    final URI resourceURI = identifiedBy(replacement.getId());
    return Response.created(resourceURI)
        .entity(asWebEntity(replacement, resourceURI))
        .build();
  }

  /**
   * Updates the replacement identified by the specified unique identifier with the given
   * replacement entity. If no such replacement exists in the requested workflow, then an HTTP
   * error {@link Response.Status#NOT_FOUND} is sent back.
   * <p>
   * Only the period over which the replacement will occur and the substitute can be updated.
   * If any other properties (the incumbent) are modified, an HTTP error
   * {@link Response.Status#BAD_REQUEST} is sent back.
   * </p>
   * <p>
   * For security reason, unless the requester plays the role of supervisor in the requested
   * workflow, only the incumbent of the tasks concerned by the replacement can update a
   * replacement. Otherwise, an HTTP error {@link Response.Status#FORBIDDEN} is sent back.
   * </p>
   * @param id the unique identifier of the a replacement in the requested workflow.
   * @param entity the new state of the replacement.
   * @return the updated replacement.
   */
  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public ReplacementEntity updateReplacement(@PathParam("id") final String id,
      final ReplacementEntity entity) {
    assertIsValid(entity);
    final Replacement<?> replacement = getReplacementById(id);
    if (!replacement.getIncumbent().getUserId().equals(entity.getIncumbent().getId())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    final Replacement<?> updatedReplacement =
        replacement.setSubstitute(getUser(entity.getSubstitute().getId()))
            .setPeriod(Period.between(entity.getStartDate(), entity.getEndDate()))
            .save();
    return asWebEntity(updatedReplacement, getUri().getAbsolutePath());
  }

  /**
   * Deletes the replacement identified by the specified unique identifier. If no such replacement
   * exists in the requested workflow, then an HTTP error {@link Response.Status#NOT_FOUND} is sent
   * back.
   * <p>
   * For security reason, unless the requester plays the role of supervisor in the requested
   * workflow, only the incumbent of the tasks concerned by the replacement or the substitute can
   * delete a replacement. Otherwise, an HTTP error {@link Response.Status#FORBIDDEN} is sent back.
   * </p>
   * @param replacementId the unique identifier of a replacement in the requested workflow.
   */
  @DELETE
  @Path("{id}")
  @Transactional
  public void deleteReplacement(@PathParam("id") final String replacementId) {
    final Replacement<?> replacement = getReplacementById(replacementId);
    replacement.delete();
  }

  @Override
  protected String getResourceBasePath() {
    return REPLACEMENT_BASE_URI.format(new Object[]{this.workflowId});
  }

  @Override
  public String getComponentId() {
    return workflowId;
  }

  private User getUser(final String userId) {
    try {
      return userManager.getUser("me".equals(userId) ? getUser().getId() : userId);
    } catch (WorkflowException e) {
      throw new WebApplicationException("User " + userId + " doesn't exist",
          Response.Status.NOT_FOUND);
    }
  }

  private <T extends Replacement<T>> T getReplacementById(final String id) {
    Optional<T> optionalReplacement = Replacement.get(id);
    T replacement = optionalReplacement.orElseThrow(
        () -> new WebApplicationException(Response.Status.NOT_FOUND));
    assertIsValid(replacement);
    return replacement;
  }

  private boolean isRequesterSupervisor() {
    boolean isSupervisor;
    try {
      isSupervisor =
          Stream.of(userManager.getUsersInRole(SilverpeasRole.SUPERVISOR.getName(), workflowId))
          .anyMatch(u -> u.getUserId().equals(getUser().getId()));
    } catch (WorkflowException e) {
      isSupervisor = false;
    }
    return isSupervisor;
  }

  private final Predicate<Replacement<?>> onTheRequester =
      r -> r.getSubstitute().getUserId().equals(getUser().getId()) ||
          r.getIncumbent().getUserId().equals(getUser().getId());

  private <T extends Replacement<T>> Stream<T> filterOnTheRequester(final Stream<T> replacements) {
    return isRequesterSupervisor() ? replacements : replacements.filter(onTheRequester);
  }

  private <T extends Replacement<T>> List<ReplacementEntity> encode(final Stream<T> replacements) {
    return replacements.map(r -> asWebEntity(r, identifiedBy(r.getId())))
        .collect(Collectors.toList());
  }

  private void assertIsValid(final ReplacementEntity entity) {
    if (entity.getIncumbent() == null || entity.getSubstitute() == null ||
        entity.getStartDate() == null || entity.getEndDate() == null ||
        !workflowId.equals(entity.getWorkflowInstanceId())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    } else if (!isRequesterSupervisor() &&
        !entity.getIncumbent().getId().equals(getUser().getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  private void assertIsValid(final Replacement<?> replacement) {
    if (!workflowId.equals(replacement.getWorkflowInstanceId())) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (!onTheRequester.test(replacement) && !isRequesterSupervisor()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

}
  