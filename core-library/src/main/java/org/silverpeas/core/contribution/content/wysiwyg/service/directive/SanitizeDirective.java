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
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.kernel.util.StringUtil;

import java.util.regex.Pattern;

import static org.owasp.html.Sanitizers.BLOCKS;
import static org.owasp.html.Sanitizers.FORMATTING;
import static org.owasp.html.Sanitizers.LINKS;
import static org.owasp.html.Sanitizers.STYLES;
import static org.owasp.html.Sanitizers.TABLES;

/**
 * Sanitize the WYSIWYG content in order to keep only:
 * <ul>
 *   <li>safe formatting</li>
 *   <li>safe blocks</li>
 *   <li>safe images</li>
 *   <li>safe links</li>
 *   <li>safe tables</li>
 *   <li>safe styles</li>
 * </ul>
 * @author silveryocha
 */
public class SanitizeDirective implements WysiwygContentTransformerDirective {

  /**
   * The raster image types accepted as an inlined image. SVG is deliberately excluded as such a
   * document can carry scripts.
   */
  private static final Pattern INLINED_IMAGE = Pattern.compile(
      "(?i)^data:image/(?:png|jpeg|gif|webp);base64,[a-z0-9+/]+={0,2}$");

  /**
   * The source of an image is either a regular HTTP(S) URL or an image directly inlined as a
   * base64 encoded data URI. The latter is used by the user notifications to carry the thumbnail
   * of a contribution, as the notification content is built once, whatever the channel by which
   * it will be then distributed.
   * <p>
   * This is a replacement of {@link org.owasp.html.Sanitizers#IMAGES} which allows only the HTTP
   * and HTTPS protocols. The protocol guard and the pattern below are combined by the policy
   * builder as a conjunction: a source has to satisfy both of them.
   * </p>
   */
  private static final PolicyFactory IMAGES = new HtmlPolicyBuilder()
      .allowUrlProtocols("http", "https", "data")
      .allowElements("img")
      .allowAttributes("alt").onElements("img")
      .allowAttributes("src")
          .matching((elementName, attributeName, value) ->
              value.startsWith("data:") && !INLINED_IMAGE.matcher(value).matches() ? null : value)
          .onElements("img")
      .allowAttributes("border", "height", "width")
          .matching(Pattern.compile("^\\d+$"))
          .onElements("img")
      .toFactory();

  private static final PolicyFactory POLICY_FACTORY =
      FORMATTING.and(BLOCKS).and(LINKS).and(STYLES).and(TABLES).and(IMAGES);

  @Override
  public String execute(final String wysiwygContent) {
    if (wysiwygContent == null) {
      return StringUtil.EMPTY;
    }
    return POLICY_FACTORY.sanitize(wysiwygContent);
  }
}
