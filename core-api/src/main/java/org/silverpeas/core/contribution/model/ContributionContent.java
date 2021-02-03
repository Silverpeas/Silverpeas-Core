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

import org.silverpeas.core.contribution.content.renderer.ContributionContentRenderer;
import org.silverpeas.core.contribution.content.renderer.ContributionContentRendererProvider;

import java.io.Serializable;

/**
 * The content of a contribution in Silverpeas. It is the more generic representation of a content
 * embedded into a user contribution. The language of the content is the language in which the
 * contribution is authored. All conceptual representation of a content supported in
 * Silverpeas should implement this interface.
 * @param <T> the type the content's data.
 * @author mmoquillon
 */
public interface ContributionContent<T> extends Serializable {

  /**
   * Gets the localized contribution to which the content is related. The language in which the
   * contribution was authored is taken as the content language.
   * @return the contribution that owns this content.
   */
  LocalizedContribution getContribution();

  /**
   * Gets the data of a content. A data can be a text, a structure, a binary stream, and so on. The
   * type of the data should be represented by a Java type.
   * @return the data of the content.
   */
  T getData();

  /**
   * Does the content is empty?
   * @return true if this content doesn't contain any data, false otherwise.
   */
  default boolean isEmpty() {
    return getData() != null;
  }

  /**
   * Gets the renderer of the content.
   * @return the {@link ContributionContentRenderer} instance.
   */
  default ContributionContentRenderer getRenderer() {
    return ContributionContentRendererProvider.get().ofContent(this);
  }
}
