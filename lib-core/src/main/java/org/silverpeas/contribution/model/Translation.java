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

package org.silverpeas.contribution.model;

/**
 * A translation of an i18n contribution or of a content in a given language. All contributions
 * or contents that represent a translation of a given contribution or content in a given language
 * should implement one of its children interfaces:
 * <ul>
 *   <li>{@code org.silverpeas.contribution.ContributionTranslation} for the contributions,</li>
 *   <li>{@¢ode org.silverpeas.contribution.model.ContributionContentTranslation} for the
 *   contribution content.</li>
 * </ul>
 * <p>
 *   For example, as the <code>Publication</code> is a i18n contribution, it must be capable to
 *   return a <code>PublicationTranslation</code> instance for each asking translation in a given
 *   language.
 * </p>
 * @author mmoquillon
 */
public interface Translation {

  public String getLanguage();

}
