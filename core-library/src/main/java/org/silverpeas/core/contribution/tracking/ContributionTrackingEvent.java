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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An event in the tracking of modifications on a given contribution. The event is about a given
 * action that was occurred on the related contribution by a given user at a given date.
 * @author mmoquillon
 */
@Entity
@Table(name = "SB_Contribution_Tracking")
public class ContributionTrackingEvent
    extends BasicJpaEntity<ContributionTrackingEvent, UuidIdentifier> {

  @Embedded
  private TrackedAction action;

  @Embedded
  private ContributionIdentifier contributionId;

  @Column(name = "context")
  private String context = "";

  protected ContributionTrackingEvent() {
    // for JPA
  }

  /**
   * Constructs a new tracking event about the specified action being performed against the given
   * contribution.
   * @param action the action that has been done.
   * @param contributionId the unique identifier of the contribution on which the action has be
   * done.
   */
  public ContributionTrackingEvent(final TrackedAction action,
      final ContributionIdentifier contributionId) {
    this.action = action;
    this.contributionId = contributionId;
  }

  /**
   * Gets a description about the context on the modification that was operated on the underlying
   * contribution.
   * @return a short description of the modification context or an empty string if no description
   * was provided about it.
   */
  public String getContext() {
    return context == null ? "" : context;
  }

  /**
   * Sets a short text explaining the context under which the tracked modification of the underlying
   * contribution was operated.
   * @param context
   * @return
   */
  public ContributionTrackingEvent setContext(final String context) {
    this.context = context;
    return this;
  }

  /**
   * Gets the action that is reported by this event.
   * @return the tracked action on the contribution related by this event.
   */
  public TrackedAction getAction() {
    return action;
  }

  /**
   * Gets the identifier of the contribution that is related by this event.
   * @return the identifier of a contribution in Silverpeas.
   */
  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Saves the specified event into the modification history of the related contribution.
   */
  public ContributionTrackingEvent save() {
    return ContributionTrackingRepository.get().save(this);
  }
}
