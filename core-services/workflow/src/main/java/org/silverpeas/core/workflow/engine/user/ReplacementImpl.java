/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;

/**
 * Implementation of the replacement business object by using JPA for the persistence.
 * @author mmoquillon
 */
@NamedQuery(name = "Replacement.findAllByIncumbentAndByWorkflow",
    query = "select r from ReplacementImpl r where r.incumbentId = :incumbent and " +
        "r.workflowId = :workflow")
@NamedQuery(name = "Replacement.findAllBySubstituteAndByWorkflow",
    query = "select r from ReplacementImpl r where r.substituteId = :substitute and " +
        "r.workflowId = :workflow")
@NamedQuery(name = "Replacement.findAllByWorkflow",
    query = "select r from ReplacementImpl r where r.workflowId = :workflow")
@NamedQuery(name = "Replacement.findAllByUsersAndByWorkflow",
    query = "select r from ReplacementImpl r where r.incumbentId = :incumbent and r" +
        ".substituteId = :substitute and r.workflowId = :workflow")
@Entity
@Table(name = "sb_workflow_replacements")
public class ReplacementImpl extends SilverpeasJpaEntity<ReplacementImpl, UuidIdentifier>
    implements Replacement<ReplacementImpl> {

  @NotNull
  @Column(nullable = false)
  private String incumbentId;

  @NotNull
  @Column(nullable = false)
  private String substituteId;

  @NotNull
  @Column(nullable = false)
  private String workflowId;

  @NotNull
  @Column(nullable = false)
  private LocalDate startDate;

  @NotNull
  @Column(nullable = false)
  private LocalDate endDate;

  @Override
  public User getIncumbent() {
    return getWorkflowUser(incumbentId);
  }

  @Override
  public User getSubstitute() {
    return getWorkflowUser(substituteId);
  }

  @Override
  public Period getPeriod() {
    return Period.between(startDate, endDate);
  }

  @Override
  public String getWorkflowInstanceId() {
    return workflowId;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  ReplacementImpl setIncumbent(final User incumbent) {
    Objects.requireNonNull(incumbent, "The user to replace must be non-null");
    requireDifferentUsers(incumbent.getUserId(), substituteId);
    this.incumbentId = incumbent.getUserId();
    return this;
  }

  @Override
  public ReplacementImpl setSubstitute(final User substitute) {
    Objects.requireNonNull(substitute, "The user who replaces the incumbent must be non-null");
    requireDifferentUsers(incumbentId, substitute.getUserId());
    this.substituteId = substitute.getUserId();
    return this;
  }

  ReplacementImpl setWorkflowId(final String workflowId) {
    StringUtil.requireDefined(workflowId,
        "The unique identifier of the workflow instance must be non-null");
    this.workflowId = workflowId;
    return this;
  }

  public ReplacementImpl setPeriod(final Period period) {
    Objects.requireNonNull(period,
        "The period during which the replacement is enabled must be non-null");
    this.startDate = asLocalDate(period.getStartDate());
    this.endDate = asLocalDate(period.getEndDate());
    return this;
  }

  private User getWorkflowUser(final String userId) {
    try {
      return WorkflowHub.getUserManager().getUser(userId);
    } catch (WorkflowException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private void requireDifferentUsers(final String incumbentId, final String substituteId) {
    if (StringUtil.isDefined(incumbentId) && StringUtil.isDefined(substituteId) &&
        incumbentId.equals(substituteId)) {
      throw new IllegalArgumentException("A user cannot be replaced by himself");
    }
  }
}
  