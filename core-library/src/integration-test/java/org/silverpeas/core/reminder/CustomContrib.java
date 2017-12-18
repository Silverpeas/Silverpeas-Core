/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * @author mmoquillon
 */
public class CustomContrib implements Contribution {

  private final ContributionIdentifier id;
  private User author;
  private User lastContributor;
  private Date creationDate;
  private Date lastContributionDate;
  private OffsetDateTime startDate = OffsetDateTime.now();

  public CustomContrib(final ContributionIdentifier id) {
    this.id = id;
  }

  public CustomContrib authoredBy(final User author) {
    if (this.author == null) {
      this.author = author;
      this.creationDate = new Date();
    }
    this.lastContributor = author;
    this.lastContributionDate = new Date();
    return this;
  }

  public OffsetDateTime nextOccurrenceSince(final OffsetDateTime dateTime) {
    return startDate;
  }

  @Override
  public ContributionIdentifier getContributionId() {
    return id;
  }

  @Override
  public User getCreator() {
    return author;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public User getLastModifier() {
    return lastContributor;
  }

  @Override
  public Date getLastModificationDate() {
    return lastContributionDate;
  }
}
  