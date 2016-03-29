/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.model;

/**
 * An internationalized contribution. It supports different languages and regional specificities
 * (i18n). Any contribution supporting the translation of some of its properties in different
 * language should implements this interface.
 *
 * @author mmoquillon
 */
public interface I18nContribution extends Contribution {

  /**
   * Gets a translation of this contribution in the specified language.
   * @param language the ISO 639-1 code of the language.
   * @return a translation of the contribution in the specified language.
   */
  <T extends ContributionTranslation> T getTranslation(String language);

}
