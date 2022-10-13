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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.silverpeas.core.util.StringUtil;

import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.util.URLUtil.getCurrentServerURL;

/**
 * Into context of a mail, silverpeas's internal link MUST be completed with the URL of the
 * Silverpeas's server.
 * @author silveryocha
 */
public class MailLinkCssApplierDirective extends AbstractDirective {

  @Override
  public void prepareReplacements(final Source source, final Map<String, String> replacements) {
    final List<Element> linkElements = source.getAllElements(HTMLElementName.A);
    for (final Element currentLink : linkElements) {
      final StartTag linkStartTag = currentLink.getStartTag();
      if (isCompliantHref(linkStartTag)) {
        final String href = getHrefAttribute(linkStartTag);
        final String newHref = getCurrentServerURL().replaceFirst("[/]+$", "") + href;
        apply(linkStartTag, newHref, replacements);
      }
    }
  }

  private void apply(final StartTag linkStartTag, final String newHref,
      final Map<String, String> replacements) {
    final String linkStartTagAsString = linkStartTag.toString();
    if (replacements.containsKey(linkStartTagAsString)) {
      return;
    }
    final String href = getHrefAttribute(linkStartTag);
    final String newLinkStartTagAsString = linkStartTagAsString.replace(href, newHref);
    replacements.put(linkStartTagAsString, newLinkStartTagAsString);
  }

  private boolean isCompliantHref(final StartTag linkStartTag) {
    return getHrefAttribute(linkStartTag).startsWith(getApplicationURL());
  }

  private String getHrefAttribute(final StartTag linkStartTag) {
    return linkStartTag.getURIAttributes()
        .stream()
        .map(Attribute::getValueSegment)
        .map(Segment::toString)
        .findFirst()
        .orElse(StringUtil.EMPTY);
  }
}