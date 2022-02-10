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
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.variables.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transforms all URL of images to take into account theirs display size.
 * @author Yohann Chastagnier
 */
public class VariablesReplacementDirective implements WysiwygContentTransformerDirective {

  @Override
  public String execute(final String wysiwygContent) {
    String wysiwygToTransform = wysiwygContent != null ? wysiwygContent : "";
    Source source = new Source(wysiwygToTransform);
    List<Element> spanElements = source.getAllElements(HTMLElementName.SPAN);
    Map<String, String> replacements = new HashMap<>();
    for (Element currentSpan : spanElements) {

      // The part that is not modified
      String spanTag = currentSpan.toString();

      String spanClass = currentSpan.getAttributeValue("class");
      if ("sp-variable".equals(spanClass)) {
        String valueId = currentSpan.getAttributeValue("rel");
        if (!replacements.containsKey(spanTag)) {
          Variable variable = Variable.getById(valueId);
          if (variable != null) {
            variable.getVariableValues().getCurrent().ifPresent(v -> {
              String newSpanTag = currentSpan.getStartTag().toString() +
                  WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(v.getValue()) +
                  currentSpan.getEndTag().toString();
              replacements.put(spanTag, newSpanTag);
            });
          }
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
}