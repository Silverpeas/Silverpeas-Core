/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.filter;

import org.owasp.html.Encoding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The opening tag of an element, strictly parsed from a text: the name of the element directly
 * follows the opening angle bracket, the attributes are separated by whitespaces, their value is
 * either well quoted or without any ambiguous character, none of them is declared twice, and the
 * tag is terminated. A tag parsed this way is necessarily read the same way by the browsers,
 * whereas they recover from a malformed one in ways that cannot be all foreseen here.
 *
 * @author mmoquillon
 */
final class OpeningTag {

  private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
      "\\G\\s+([^\\s\"'>/=]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+)))?");
  private static final Pattern TAG_END_PATTERN = Pattern.compile("\\G\\s*/?>");

  private final Map<String, String> attributes;
  private final int end;

  private OpeningTag(final Map<String, String> attributes, final int end) {
    this.attributes = attributes;
    this.end = end;
  }

  /**
   * Parses the opening tag of the specified element at the given position in the text.
   *
   * @param element the name of the element whose opening tag is expected.
   * @param text the text to parse.
   * @param tagStart the position in the text of the opening angle bracket of the tag.
   * @return the opening tag, or nothing if no such a tag can be strictly parsed at this position.
   */
  static Optional<OpeningTag> of(final String element, final String text, final int tagStart) {
    final String opening = "<" + element;
    if (!text.regionMatches(true, tagStart, opening, 0, opening.length())) {
      return Optional.empty();
    }
    final Map<String, String> attributes = new HashMap<>();
    int position = tagStart + opening.length();
    final Matcher attribute = ATTRIBUTE_PATTERN.matcher(text).region(position, text.length());
    while (attribute.find()) {
      final String name = attribute.group(1).toLowerCase(Locale.ROOT);
      if (attributes.containsKey(name)) {
        return Optional.empty();
      }
      attributes.put(name, valueOf(attribute));
      position = attribute.end();
    }
    final Matcher end = TAG_END_PATTERN.matcher(text).region(position, text.length());
    return end.lookingAt() ? Optional.of(new OpeningTag(attributes, end.end())) : Optional.empty();
  }

  /**
   * Is the specified attribute declared by this tag?
   *
   * @param name the name of an attribute, in lower case.
   * @return true if the attribute is declared, whatever its value. False otherwise.
   */
  boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  /**
   * Gets the value of the specified attribute as the browsers read it, that is to say with its
   * HTML entities decoded. The decoding is the one of the HTML tokenizer of the OWASP sanitizer,
   * by which the contents being rendered are read: unlike a decoding of the sole entities of HTML
   * 4, it knows all the entities the browsers know and it decodes them even when they are loosely
   * written, without their ending semicolon for example. So a value cannot be read here in a way
   * and by the browsers in another one.
   *
   * @param name the name of an attribute, in lower case.
   * @return the value of the attribute, empty if the attribute is declared without any value, or
   * null if the attribute isn't declared.
   */
  String getAttribute(final String name) {
    final String value = attributes.get(name);
    return value == null ? null : Encoding.decodeHtml(value, true);
  }

  /**
   * Gets the position in the text of the character following this tag.
   *
   * @return the position just after the closing angle bracket of the tag.
   */
  int getEnd() {
    return end;
  }

  private static String valueOf(final Matcher attribute) {
    for (int group = 2; group <= 4; group++) {
      if (attribute.group(group) != null) {
        return attribute.group(group);
      }
    }
    return "";
  }
}
