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

package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.wysiwyg.service.directive.ImageUrlAccordingToHtmlSizeDirective;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author silveryocha
 */
@Service
public class SimpleDocumentUrlAccordingToHtmlSizeDirectiveTranslator
    implements ImageUrlAccordingToHtmlSizeDirective.SrcTranslator {

  @Override
  public boolean isCompliantUrl(final String url) {
    return defaultStringIfNotDefined(url).contains("/attachmentId/");
  }

  @Override
  public String translateUrl(final String url, final String width, final String height) {
    // Computing the new src URL
    // at first, removing the size from the URL
    String  newUrl = url.replaceFirst("(?i)/size/[0-9 x]+", "");
    // then guessing the new src URL
    StringBuilder sizeUrlPart = new StringBuilder().append(width).append("x").append(height);
    if (sizeUrlPart.length() > 1) {
      sizeUrlPart.insert(0, "/size/");
      newUrl = newUrl.replaceFirst("/name/", sizeUrlPart + "/name/");
    }
    return newUrl;
  }
}
