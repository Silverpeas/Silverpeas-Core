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

import java.util.List;
import java.util.Map;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.util.URLUtil.isPermalink;

/**
 * Applies sp-permalink or sp-link CSS classes to links.
 * @author silveryocha
 */
public class SilverpeasLinkCssApplierDirective extends AbstractDirective {

  @Override
  public void prepareReplacements(final Source source, final Map<String, String> replacements) {
    final List<Element> linkElements = source.getAllElements(HTMLElementName.A);
    for (final Element currentLink : linkElements) {
      final StartTag linkStartTag = currentLink.getStartTag();
      if (isCompliantTarget(linkStartTag)) {
        final String href = defaultStringIfNotDefined(currentLink.getAttributeValue("href"));
        if (isPermalink(href)) {
          apply(linkStartTag, "sp-permalink", replacements);
        } else if (href.contains(getApplicationURL())) {
          apply(linkStartTag, "sp-link", replacements);
        }
      }
    }
  }

  private void apply(final StartTag linkStartTag, final String cssClass,
      final Map<String, String> replacements) {
    modifyElementAttribute(linkStartTag, "class", c -> {
      if (c == null) {
        return cssClass;
      } else {
        final String cleanedCssClasses = c.replaceAll("[ ]*(sp-permalink|sp-link)", "").trim();
        final String cssClassToAdd = cleanedCssClasses.isEmpty() ? cssClass : (" " + cssClass);
        return cleanedCssClasses + cssClassToAdd;
      }
    }, replacements);
  }

  private boolean isCompliantTarget(final StartTag linkStartTag) {
    return isNotDefined(linkStartTag.getAttributeValue("target"));
  }
}