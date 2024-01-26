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
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.silverpeas.kernel.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * Modify the content in order to make that links are opened on a blank page.
 * @author silveryocha
 */
public class OpenLinkOnBlankPageDirective extends AbstractDirective {

  @Override
  public void prepareReplacements(final Source source, final Map<String, String> replacements) {
    final List<Element> linkElements = source.getAllElements(HTMLElementName.A);
    for (final Element currentLink : linkElements) {
      final StartTag linkStartTag = currentLink.getStartTag();
      modifyElementAttributes(linkStartTag, List.of(
          Pair.of("target", c -> "_blank"),
          Pair.of("rel", c -> "noopener noreferrer nofollow")), replacements);
    }
  }
}