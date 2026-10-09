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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks a value against the XSS injections detectable by a pattern: the forbidden elements, the
 * event callback attributes and the scripting schemes. The iframes and the scripts, which are
 * allowed under conditions, are the matter of their own checker.
 *
 * @author mmoquillon
 */
final class XssPatternChecker implements SecurityChecker {

  private static final List<XssPattern> PATTERNS = List.of(
      new XssPattern("(?i)<[\\s/]*(svg|math|details)", "a forbidden element"),
      // an event callback declaration isn't necessarily preceded by a whitespace: according to the
      // HTML tokenizer, the solidus and the closing quote of the previous attribute value both lead
      // back to the state at which an attribute name is expected. So "<img src="x"onerror=..." does
      // declare an onerror callback and browsers do run it.
      new XssPattern("[\\s/\"']on\\w+\\s*=", "an event callback attribute"),
      // a scripting scheme given as the value of an attribute, such as the formaction of a button
      // or the href of a link. The colon is written here as the browsers decode it, that is to say
      // as the character itself or as any of the HTML entities standing for it. The data scheme is
      // deliberately left out: the contents do embed inlined images with it.
      new XssPattern("(?i)[\\s/\"'][\\w:-]+\\s*=\\s*[\"']?\\s*(?:java|vb)script\\s*" +
          "(?::|&colon;|&#0*58;|&#x0*3a;)", "a scripting scheme as an attribute value"));

  @Override
  public Optional<String> rejectionIn(final String value) {
    for (final XssPattern pattern : PATTERNS) {
      final Matcher matcher = pattern.pattern.matcher(value);
      if (matcher.find()) {
        return SecurityChecker.rejection(pattern.description, value, matcher.start());
      }
    }
    return Optional.empty();
  }

  private static final class XssPattern {
    private final Pattern pattern;
    private final String description;

    private XssPattern(final String regexp, final String description) {
      this.pattern = Pattern.compile(regexp);
      this.description = description;
    }
  }
}
