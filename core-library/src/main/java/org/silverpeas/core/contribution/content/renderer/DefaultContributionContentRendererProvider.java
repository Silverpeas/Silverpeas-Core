/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;

/**
 * Default implementation of {@link ContributionContentRendererProvider}.
 * @author silveryocha
 */
@Singleton
public class DefaultContributionContentRendererProvider
    implements ContributionContentRendererProvider {

  private static final String NAME_SUFFIX = "Renderer";

  @Override
  public AbstractContributionRenderer ofContent(final ContributionContent content) {
    final String contentClassName = content.getClass().getSimpleName();
    final AbstractContributionRenderer renderer = ServiceProvider.getService(
        contentClassName.substring(0, 1).toLowerCase() + contentClassName.substring(1) +
            NAME_SUFFIX);
    renderer.setContent(content);
    return renderer;
  }
}
