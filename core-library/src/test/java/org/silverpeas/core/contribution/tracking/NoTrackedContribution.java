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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * A contribution that doesn't support tracking for modifications. It is dedicated to the unit
 * tests.
 * @author mmoquillon
 */
public class NoTrackedContribution implements Contribution {

  private final ContributionIdentifier id;
  private final Date creationDate;
  private Date lastUpdateDate;
  private final User creator;
  private User updater;

  public NoTrackedContribution(final ContributionIdentifier id, final OffsetDateTime creationDate,
      final User creator) {
    this.id = id;
    this.creationDate = Date.from(creationDate.toInstant());
    this.creator = creator;
    this.updater = this.creator;
    this.lastUpdateDate = this.creationDate;
  }

  public NoTrackedContribution(final NoTrackedContribution contribution) {
    this.id = contribution.id;
    this.creationDate = contribution.creationDate;
    this.creator = contribution.creator;
    this.updater = contribution.updater;
    this.lastUpdateDate = contribution.lastUpdateDate;
  }

  public NoTrackedContribution update(final OffsetDateTime updateDate, final User updater) {
    NoTrackedContribution updated = new NoTrackedContribution(this);
    updated.lastUpdateDate = Date.from(updateDate.toInstant());
    updated.updater = updater;
    return updated;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  @Override
  public User getCreator() {
    return creator;
  }

  @Override
  public User getLastUpdater() {
    return updater;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return id;
  }
}
