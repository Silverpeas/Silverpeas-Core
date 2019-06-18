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
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.util.URLUtil.isPermalink;

/**
 * Applies sp-permalink or sp-link CSS classes to links.
 * @author silveryocha
 */
public class SilverpeasLinkCssApplierDirective implements WysiwygContentTransformerDirective {

  @Override
  public String execute(final String wysiwygContent) {
    final String wysiwygToTransform = wysiwygContent != null ? wysiwygContent : "";
    final Source source = new Source(wysiwygToTransform);
    final List<Element> linkElements = source.getAllElements(HTMLElementName.A);
    final Map<String, String> replacements = new HashMap<>();
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

    String transformedWysiwygContent = wysiwygToTransform;
    for (Map.Entry<String, String> replacement : replacements.entrySet()) {
      transformedWysiwygContent = transformedWysiwygContent.replace(replacement.getKey(),
          replacement.getValue());
    }

    // Returning the transformed WYSIWYG.
    return transformedWysiwygContent;
  }

  private void apply(final StartTag linkStartTag, final String cssClass, final Map<String, String> replacements) {
    final String linkStartTagAsString = linkStartTag.toString();
    if (replacements.containsKey(linkStartTagAsString)) {
      return;
    }
    final String currentCssClasses = linkStartTag.getAttributeValue("class");
    final String newLinkStartTagAsString;
    if (currentCssClasses == null) {
      newLinkStartTagAsString = linkStartTagAsString
          .replaceFirst("([ \t\r\n]*)>$", " class=\"" + cssClass + "\"$1>");
    } else {
      final String cleanedCssClasses = currentCssClasses.replaceAll("[ ]*(sp-permalink|sp-link)", "").trim();
      final String cssClassToAdd = cleanedCssClasses.isEmpty() ? cssClass : (" " + cssClass);
      final String newCssClasses = cleanedCssClasses + cssClassToAdd;
      newLinkStartTagAsString = linkStartTagAsString.replaceAll(
          "(class[ \t\r\n]*=[ \t\r\n]*([\"'])[ \t\r\n]*)" + currentCssClasses +
              "([ \t\r\n]*([\"']))", "$1" + newCssClasses + "$2");
    }
    replacements.put(linkStartTagAsString, newLinkStartTagAsString);
  }

  private boolean isCompliantTarget(final StartTag linkStartTag) {
    return isNotDefined(linkStartTag.getAttributeValue("target"));
  }
}