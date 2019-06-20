/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.contribution.content.renderer;

import org.silverpeas.core.contribution.model.ContributionContent;

import java.io.Serializable;

/**
 * The renderer of a {@link ContributionContent}. Each implementation of
 * {@link ContributionContent} MUST provide a renderer by the method
 * {@link ContributionContent#getRenderer()}.
 * <p>
 * According to the context of content rendering, the content could be more or less transformed.
 * </p>
 * @author silveryocha
 */
public interface ContributionContentRenderer extends Serializable {

  /**
   * Rendering the HTML content in order to be displayed into a context of edition.
   * @return HTML as {@link String}.
   */
  String renderView();

  /**
   * Rendering the HTML content in order to be displayed into a context of edition.
   * @return HTML as {@link String}.
   */
  String renderEdition();
}
