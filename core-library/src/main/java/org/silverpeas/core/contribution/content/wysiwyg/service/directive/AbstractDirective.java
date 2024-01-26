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

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Centralization of code.
 * @author silveryocha
 */
public abstract class AbstractDirective implements WysiwygContentTransformerDirective {

  @Override
  public String execute(final String wysiwygContent) {
    if (wysiwygContent == null) {
      return StringUtil.EMPTY;
    }
    final Source source = new Source(wysiwygContent);
    final Map<String, String> replacements = new HashMap<>();
    prepareReplacements(source, replacements);
    String transformedWysiwygContent = wysiwygContent;
    for (final Map.Entry<String, String> replacement : replacements.entrySet()) {
      transformedWysiwygContent = transformedWysiwygContent.replace(replacement.getKey(),
          replacement.getValue());
    }
    return transformedWysiwygContent;
  }

  /**
   * Prepares all replacements to perform.
   * @param source the initial source content.
   * @param replacements the map into which replacements MUST be specified. The key represents
   * the occurrence into source, the value the replacements.
   */
  protected abstract void prepareReplacements(final Source source,
      final Map<String, String> replacements);

  /**
   * Centralizes the modification of the value of an attribute.
   * @param linkStartTag the {@link StartTag} instance of current element.
   * @param attrName the name of aimed attribute to modify.
   * @param newValue the {@link Function} implementation taking as parameter the current value of
   * attribute and returns the modified one. Null parameter means that attribute does not yet exist.
   * @param replacements the map into which replacements MUST be specified. The key represents
   * the occurrence into source, the value the replacements.
   */
  protected void modifyElementAttribute(final StartTag linkStartTag, final String attrName,
      final UnaryOperator<String> newValue, final Map<String, String> replacements) {
    modifyElementAttributes(linkStartTag, List.of(Pair.of(attrName, newValue)), replacements);
  }

  /**
   * Centralizes the modification of the value of an attribute.
   * @param linkStartTag the {@link StartTag} instance of current element.
   * @param attrDirectives collection of attributes directives. Each directive is represented by
   * a {@link Pair} instance which on left is the name of the attribute and on the right the
   * {@link Function} implementation taking as parameter the current value of attribute and
   * returns the modified one. Null parameter means that attribute does not yet exist.
   * @param replacements the map into which replacements MUST be specified. The key represents
   * the occurrence into source, the value the replacements.
   */
  protected void modifyElementAttributes(final StartTag linkStartTag,
      final Collection<Pair<String, UnaryOperator<String>>> attrDirectives,
      final Map<String, String> replacements) {
    final String startTagAsString = linkStartTag.toString();
    if (replacements.containsKey(startTagAsString)) {
      return;
    }
    String currentStartTagAsString = startTagAsString;
    for (final Pair<String, UnaryOperator<String>> attrDirective : attrDirectives) {
      final String attrName = attrDirective.getFirst();
      final UnaryOperator<String> newValue = attrDirective.getSecond();
      final String currentAttrValue = linkStartTag.getAttributeValue(attrName);
      if (currentAttrValue == null) {
        currentStartTagAsString = currentStartTagAsString
            .replaceFirst("([ \t\r\n]*)>$", " " + attrName + "=\"" + newValue.apply(null) + "\"$1>");
      } else {
        currentStartTagAsString = currentStartTagAsString.replaceAll(
            "(" + attrName + "[ \t\r\n]*=[ \t\r\n]*([\"'])[ \t\r\n]*)" + currentAttrValue +
                "([ \t\r\n]*([\"']))", "$1" + newValue.apply(currentAttrValue) + "$2");
      }
    }
    replacements.put(startTagAsString, currentStartTagAsString);
  }
}