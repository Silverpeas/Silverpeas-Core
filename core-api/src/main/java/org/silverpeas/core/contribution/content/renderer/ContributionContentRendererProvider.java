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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.content.renderer;

import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Named;

/**
 * In charge of providing {@link ContributionContentRenderer} as implementations of renderer
 * are existing at most of time into UI modules but not at the API level.
 * @author silveryocha
 */
public interface ContributionContentRendererProvider {

  /**
   * Gets the instance of the implementation of the interface.
   * @return an implementation of {@link ContributionContentRendererProvider}.
   */
  static ContributionContentRendererProvider get() {
    return ServiceProvider.getService(ContributionContentRendererProvider.class);
  }

  /**
   * Gets the {@link ContributionContentRenderer} instance according to given
   * {@link ContributionContent}.
   * <p>
   * This method offers to provide an instance by observing following convention of
   * renderer naming: <br/>
   * <code>
   * [simple class name of ContributionContent, with first letter turn into lower case]Renderer
   * </code><br/>
   * <code>wysiwygContentRenderer</code> for example, where
   * <code>wysiwygContent</code> is computed from {@link ContributionContent} simple class name
   * and 'Renderer' is the suffix.
   * </p>
   * <p>
   * To be provided by this way, an implementation must use {@link Named}
   * annotation and fill {@link Named#value()} in case where the implementation class name does not
   * correspond to the above naming convention.
   * </p>
   * @param content the content to render.
   * @return the {@link ContributionContentRenderer} instance.
   */
  AbstractContributionRenderer ofContent(ContributionContent content);
}
