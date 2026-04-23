/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.*;
import org.silverpeas.core.i18n.AbstractI18NBean;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * A contribution with at least one single content that is managed by a
 * {@link SilverpeasContentManager} and that is eligible to the classification onto the PdC. This
 * class builds a bridge between the new contribution API and the older one for the content managers
 * (which are part of the old API). The classification of the user contributions onto the PdC is
 * again based upon the old API.
 */
public class ManagedContribution implements SilverpeasContent, WithURL {

  private final Contribution wrappedInstance;
  private final ContributionIdentifier contributionId;
  private LocalizedContribution contribution;
  private final String icon;

  /**
   * Decorates the specified contribution as a managed contribution by a content manager whose
   * icon is defined by the specified icon file name.
   *
   * @param contribution a user contribution in Silverpeas.
   * @param iconFileName the file name of the contribution icon.
   */
  ManagedContribution(final Contribution contribution, final String iconFileName) {
    this.contributionId = contribution.getIdentifier();
    this.wrappedInstance = contribution;
    this.contribution = LocalizedContribution.from(contribution);
    this.icon = iconFileName;
  }

  private void setRightLocalizedContribution(String language) {
    if (!language.equals(contribution.getLanguage())) {
      contribution = LocalizedContribution.from(contribution, language);
    }
  }

  public Contribution getWrappedContribution() {
    return wrappedInstance;
  }

  @Override
  public String getId() {
    return contributionId.getLocalId();
  }

  @Override
  public String getName() {
    return contribution.getTitle();
  }

  public String getName(final String language) {
    if (wrappedInstance instanceof AbstractI18NBean) {
      return ((AbstractI18NBean<?>) wrappedInstance).getName(language);
    }
    setRightLocalizedContribution(language);
    return contribution.getTitle();
  }

  public String getDescription(final String language) {
    if (wrappedInstance instanceof AbstractI18NBean) {
      return ((AbstractI18NBean<?>) wrappedInstance).getDescription(language);
    }
    setRightLocalizedContribution(language);
    return contribution.getDescription();
  }

  public String getIcon() {
    return icon;
  }

  @Override
  public String getComponentInstanceId() {
    return contributionId.getComponentInstanceId();
  }

  @Override
  public String getURL() {
    if (wrappedInstance instanceof WithURL) {
      return ((WithURL) wrappedInstance).getURL();
    }
    // Indeed, the URL into context of PDC result is not used for now...
    return null;
  }

  @Override
  public User getCreator() {
    return contribution.getCreator();
  }

  @Override
  public User getLastUpdater() {
    return contribution.getLastUpdater();
  }

  @Override
  public Date getCreationDate() {
    return contribution.getCreationDate();
  }

  @Override
  public Date getLastUpdateDate() {
    return contribution.getLastUpdateDate();
  }

  @Override
  public String getSilverpeasContentId() {
    if (wrappedInstance instanceof SilverpeasContent) {
      return ((SilverpeasContent) wrappedInstance).getSilverpeasContentId();
    }
    return "";
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return contributionId;
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
    return contributionId.getType();
  }

  @Override
  public boolean isIndexable() {
    return contribution.isIndexable();
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return contribution.canBeAccessedBy(user);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return contribution.canBeModifiedBy(user);
  }

  @Override
  public boolean canBeDeletedBy(final User user) {
    return contribution.canBeDeletedBy(user);
  }

  public Collection<String> getLanguages() {
    if (wrappedInstance instanceof AbstractI18NBean) {
      return ((AbstractI18NBean<?>) wrappedInstance).getLanguages();
    }
    return Collections.emptyList();
  }
}
  