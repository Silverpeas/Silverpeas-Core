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

package org.silverpeas.core.contribution.content.wysiwyg.service;

import org.silverpeas.core.contribution.content.renderer.AbstractContributionRenderer;
import org.silverpeas.core.contribution.content.renderer.ContributionContentRendererProvider;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.contribution.model.WysiwygContent;

import javax.inject.Named;

/**
 * This renderer is instantiated as explained into
 * {@link ContributionContentRendererProvider#ofContent(ContributionContent)}
 * documentation.
 * @author silveryocha
 */
@Named
public class WysiwygContentRenderer extends AbstractContributionRenderer<WysiwygContent> {
  private static final long serialVersionUID = -5283748624108237499L;

  @Override
  public String renderView() {
    return WysiwygContentTransformer
        .on(getContent().getData())
        .modifyImageUrlAccordingToHtmlSizeDirective()
        .resolveVariablesDirective()
        .applySilverpeasLinkCssDirective()
        .transform();
  }

  @Override
  public String renderEdition() {
    return getContent().getData();
  }
}
