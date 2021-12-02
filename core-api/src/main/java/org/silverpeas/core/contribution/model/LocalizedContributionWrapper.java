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
import org.silverpeas.core.i18n.ResourceTranslation;

import java.util.Date;

/**
 * Implementation by default of a {@link LocalizedContribution} for a {@link Contribution}
 * object. It is a generic representation of a contribution, whatever it is, localized in a given
 * language. If the contribution is an i18n one, then the {@link LocalizedContributionWrapper}
 * object will represent the version of the contribution in the given language. Otherwise, the
 * {@link LocalizedContributionWrapper} object will be a localized representation of the
 * contribution like expressed in the default language of the platform.
 * @author silveryocha
 */
class LocalizedContributionWrapper implements LocalizedContribution {

  private final Contribution contribution;
  private final ResourceTranslation translation;

  LocalizedContributionWrapper(final Contribution contribution, final String language) {
    this.contribution = contribution;
    if (contribution instanceof I18nContribution) {
      this.translation = ((I18nContribution)contribution).getTranslation(language);
    } else {
      this.translation = new TranslationWrapper(language, contribution);
    }
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return contribution.getIdentifier();
  }

  @Override
  public String getTitle() {
    return translation.getName();
  }

  @Override
  public String getDescription() {
    return translation.getDescription();
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
    return translation.getLanguage();
  }

  private static class TranslationWrapper implements ResourceTranslation {

    private final String language;
    private final Contribution contribution;

    private TranslationWrapper(final String language, final Contribution contribution) {
      this.language = language;
      this.contribution = contribution;
    }

    @Override
    public String getId() {
      return contribution.getIdentifier().asString();
    }

    @Override
    public String getName() {
      return contribution.getName();
    }

    @Override
    public String getDescription() {
      return contribution.getDescription();
    }

    @Override
    public String getLanguage() {
      return language;
    }
  }
}
