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
package org.silverpeas.core.contribution.model;

import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.i18n.Translation;

/**
 * A contribution produced in a given l10n (Localisation). Such contributions are authored in a
 * given language. They can be either a non-i18n contribution, that is a contribution that is always
 * produced in the default language of Silverpeas, or a translation of a i18 contribution for a
 * given language.
 * <p>
 * Any contributions in Silverpeas that are a translation of a i18n contribution in a given
 * localization (id est language) should implement this interface.
 * </p>
 * @author mmoquillon
 */
public interface LocalizedContribution extends Contribution, Translation {

  /**
   * Decorates the specified contribution with the support of l10n. By default, the
   * language will be the default language in Silverpeas.
   * @param contribution a contribution.
   * @return a localized contribution.
   */
  static LocalizedContribution from(final Contribution contribution) {
    return from(contribution, I18n.get().getDefaultLanguage());
  }

  /**
   * Decorates the specified contribution with the support of l10n and as authored in the specified
   * language.
   * @param contribution a contribution.
   * @param language the language in which the contribution was authored in the case of an i18n
   * Silverpeas.
   * @return a contribution localized in the specified language.
   */
  static LocalizedContribution from(final Contribution contribution, final String language) {
    return new LocalizedContributionWrapper(contribution, language);
  }

  /**
   * Gets the language in which this contribution was authored. By default, returns the default
   * language in Silverpeas.
   * @return the language at which this contribution was authored.
   */
  @Override
  default String getLanguage() {
    return I18n.get().getDefaultLanguage();
  }

}
