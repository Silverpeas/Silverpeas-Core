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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.admin.user.model.User;

import java.util.Date;

/**
 * @author silveryocha
 */
class LocalizedContributionWrapper implements LocalizedContribution {
  private final Contribution contribution;
  private final String language;

  LocalizedContributionWrapper(final Contribution contribution, final String language) {
    this.contribution = contribution;
    this.language = language;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return contribution.getIdentifier();
  }

  @Override
  public String getTitle() {
    return contribution.getTitle();
  }

  @Override
  public String getDescription() {
    return contribution.getDescription();
  }

  @Override
  public String getContributionType() {
    return contribution.getContributionType();
  }

  @Override
  public boolean isIndexable() {
    return contribution.isIndexable();
  }

  @Override
  public User getCreator() {
    return contribution.getCreator();
  }

  @Override
  public Date getCreationDate() {
    return contribution.getCreationDate();
  }

  @Override
  public User getLastUpdater() {
    return contribution.getLastUpdater();
  }

  @Override
  public Date getLastUpdateDate() {
    return contribution.getLastUpdateDate();
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return contribution.canBeAccessedBy(user);
  }

  @Override
  public String getLanguage() {
    return language;
  }
}
