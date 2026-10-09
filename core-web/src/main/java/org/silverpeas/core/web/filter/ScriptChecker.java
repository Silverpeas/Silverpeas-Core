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

import org.silverpeas.core.security.html.EmbeddedSourceValidator;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the script elements present in a given text. A script is allowed only if it is a
 * well-formed opening tag whose single src attribute refers an allowed source, directly followed
 * by its closing tag: the code of a script is loaded, never carried by the script itself. As for
 * the iframes, an allowed source is either an HTTPS URL on one of the allowed hosts or a relative
 * URL within the platform, but for the files attached to the contributions which have to be
 * explicitly allowed (see {@link EmbeddedSourceValidator}). Any script that cannot be strictly
 * parsed is rejected, as is any closing tag out of an allowed script.
 *
 * @author mmoquillon
 */
final class ScriptChecker implements SecurityChecker {

  private static final String SCRIPT = "script";
  private static final String SRC = "src";
  private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<[\\s/]*script");
  private static final Pattern CLOSING_TAG_PATTERN = Pattern.compile("(?i)\\G\\s*</script\\s*>");

  private final EmbeddedSourceValidator sourceValidator;

  /**
   * Constructs a checker of scripts accepting only those loaded from the specified hosts or from
   * the specified application.
   *
   * @param allowedHosts the hosts from which a script can be loaded.
   * @param applicationPath the path of the Silverpeas application (for example /silverpeas).
   * @param attachedFilesAllowed is a script allowed to be loaded from a file attached to a
   * contribution?
   */
  ScriptChecker(final Collection<String> allowedHosts, final String applicationPath,
      final boolean attachedFilesAllowed) {
    this.sourceValidator =
        new EmbeddedSourceValidator(allowedHosts, applicationPath, attachedFilesAllowed);
  }

  /**
   * Gets the reason for which the first non-allowed script in the given text is rejected.
   *
   * @param text the text to check.
   * @return the reason of the rejection, with an excerpt of the script, or nothing if there is no
   * script in the text or if all of them are allowed.
   */
  @Override
  public Optional<String> rejectionIn(final String text) {
    final Matcher script = SCRIPT_PATTERN.matcher(text);
    int position = 0;
    while (script.find(position)) {
      final int tagStart = script.start();
      if (script.group().contains("/")) {
        return rejection("a closing script tag out of an allowed script", text, tagStart);
      }
      final Optional<OpeningTag> tag = OpeningTag.of(SCRIPT, text, tagStart);
      if (tag.isEmpty()) {
        return rejection("a script that cannot be strictly parsed", text, tagStart);
      }
      if (tag.get().hasEventHandlerAttribute()) {
        return rejection("a script with an event handler attribute", text, tagStart);
      }
      if (!sourceValidator.isAllowed(tag.get().getAttribute(SRC))) {
        return rejection("the source of a script isn't allowed", text, tagStart);
      }
      final Matcher closing =
          CLOSING_TAG_PATTERN.matcher(text).region(tag.get().getEnd(), text.length());
      if (!closing.lookingAt()) {
        return rejection("a script not immediately closed (carrying code or missing its closing " +
            "tag)", text, tagStart);
      }
      position = closing.end();
    }
    return Optional.empty();
  }

  private static Optional<String> rejection(final String reason, final String text,
      final int tagStart) {
    return SecurityChecker.rejection(reason, text, tagStart);
  }
}
