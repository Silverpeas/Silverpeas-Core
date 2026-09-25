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

import org.owasp.html.Handler;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.core.security.html.EmbeddedSourceValidator;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Sanitizes a WYSIWYG content which is about to be rendered or exported.
 * <p>
 * Unlike {@link SanitizeDirective}, which keeps only a restricted set of safe elements, this one
 * keeps the content as it is and drops only what can act on the visitor's browser. This is
 * deliberate: the WYSIWYG editor of Silverpeas is configured to accept any content
 * (<code>config.allowedContent = true</code>), so an allow list applied at rendering time would be
 * narrower than what the users are entitled to write and would silently damage their contents. Are
 * dropped:
 * </p>
 * <ul>
 *   <li>the elements able to run code or to take over the document, along with their content;</li>
 *   <li>the event callback attributes, whatever the element carrying them;</li>
 *   <li>the attributes referring a URL with a scripting scheme;</li>
 *   <li>the iframes and the media whose source isn't allowed, according to the very rule applied
 *   by the filtering of the incoming requests.</li>
 * </ul>
 * <p>
 * The parsing is delegated to the HTML tokenizer of the OWASP sanitizer, so the content is read the
 * way a browser reads it and the dropping can't be dodged by playing with the HTML syntax.
 * </p>
 * @author mmoquillon
 */
public class SanitizeForRenderingDirective implements WysiwygContentTransformerDirective {

  private static final String IFRAME = "iframe";
  private static final String SRC = "src";
  private static final String POSTER = "poster";
  private static final String SRCDOC = "srcdoc";
  private static final String EVENT_CALLBACK_PREFIX = "on";

  /**
   * The elements having no content and hence no closing tag to expect.
   */
  private static final Set<String> VOID_ELEMENTS = setOf("area", "base", "br", "col", "embed",
      "frame", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");

  /**
   * The elements dropped along with their content: they either run code or take over the document
   * the content is rendered into.
   */
  private static final Set<String> FORBIDDEN_ELEMENTS = setOf("applet", "base", "embed", "frame",
      "frameset", "link", "math", "meta", "object", "script", "svg");

  private static final Set<String> MEDIA_ELEMENTS = setOf("audio", "img", "source", "track",
      "video");

  private static final Set<String> URL_ATTRIBUTES = setOf("action", "background", "cite", "data",
      "formaction", "href", "longdesc", POSTER, "src");


  private static final Pattern SCRIPTING_SCHEME =
      Pattern.compile("(?i)^\\s*(javascript|vbscript|livescript|mocha|about)\\s*:");
  private static final Pattern DATA_SCHEME = Pattern.compile("(?i)^\\s*data\\s*:");
  private static final Pattern INLINED_IMAGE =
      Pattern.compile("(?i)^\\s*data:image/(png|jpeg|jpg|gif|webp|bmp)\\s*;\\s*base64\\s*,");

  @Override
  public String execute(final String wysiwygContent) {
    if (wysiwygContent == null) {
      return StringUtil.EMPTY;
    }
    final StringBuilder sanitized = new StringBuilder(wysiwygContent.length());
    final HtmlStreamRenderer renderer =
        HtmlStreamRenderer.create(sanitized, Handler.DO_NOTHING);
    HtmlSanitizer.sanitize(wysiwygContent, new RenderingPolicy(renderer));
    return sanitized.toString();
  }

  private static Set<String> setOf(final String... values) {
    return new HashSet<>(Arrays.asList(values));
  }



  /**
   * Passes the parsing events through, but for the ones that have to be dropped. When an element
   * is dropped along with its content, the events are skipped until the closing tag of that very
   * element: the tokenizer emits a closing tag only for the elements it has actually balanced, so
   * counting the depth of any opened element would be wrong.
   */
  private static class RenderingPolicy implements HtmlSanitizer.Policy {

    private final HtmlStreamEventReceiver output;
    private final EmbeddedSourceValidator iframeSources = iframeSources();
    private final EmbeddedSourceValidator mediaSources = mediaSources();
    private String skippedElement = null;
    private int skippedDepth = 0;

    RenderingPolicy(final HtmlStreamEventReceiver output) {
      this.output = output;
    }

    @Override
    public void openDocument() {
      output.openDocument();
    }

    @Override
    public void closeDocument() {
      output.closeDocument();
    }

    @Override
    public void openTag(final String elementName, @NonNull final List<String> attributes) {
      final String element = elementName.toLowerCase(Locale.ROOT);
      if (skippedElement != null) {
        if (skippedElement.equals(element)) {
          skippedDepth++;
        }
        return;
      }
      if (FORBIDDEN_ELEMENTS.contains(element) || isEmbeddingANonAllowedSource(element,
          attributes)) {
        skip(element);
        return;
      }
      output.openTag(element, keepHarmlessAttributes(attributes));
    }

    @Override
    public void closeTag(final String elementName) {
      final String element = elementName.toLowerCase(Locale.ROOT);
      if (skippedElement != null) {
        if (skippedElement.equals(element) && --skippedDepth == 0) {
          skippedElement = null;
        }
        return;
      }
      output.closeTag(element);
    }

    @Override
    public void text(@NonNull final String textChunk) {
      if (skippedElement == null) {
        output.text(textChunk);
      }
    }

    private void skip(final String element) {
      if (!VOID_ELEMENTS.contains(element)) {
        skippedElement = element;
        skippedDepth = 1;
      }
    }

    private boolean isEmbeddingANonAllowedSource(final String element,
        final List<String> attributes) {
      final String src = valueOf(attributes);
      if (IFRAME.equals(element)) {
        // an iframe without any source has nothing to embed
        return !iframeSources.isAllowed(src);
      }
      // a media can carry its source by a child source element instead of by its own attribute
      return MEDIA_ELEMENTS.contains(element) && src != null && isNotAllowedMedia(src);
    }

    private static EmbeddedSourceValidator iframeSources() {
      return new EmbeddedSourceValidator(SecuritySettings.getAllowedHostsForIFrame(),
          URLUtil.getApplicationURL());
    }

    private static EmbeddedSourceValidator mediaSources() {
      return new EmbeddedSourceValidator(SecuritySettings.getAllowedHostsForMedia(),
          URLUtil.getApplicationURL());
    }

    private boolean isNotAllowedMedia(final String src) {
      return !INLINED_IMAGE.matcher(src).find() && !mediaSources.isAllowed(src);
    }

    private List<String> keepHarmlessAttributes(final List<String> attributes) {
      final List<String> kept = new java.util.ArrayList<>(attributes.size());
      for (int i = 0; i < attributes.size() - 1; i += 2) {
        final String name = attributes.get(i).toLowerCase(Locale.ROOT);
        final String value = attributes.get(i + 1);
        if (name.startsWith(EVENT_CALLBACK_PREFIX) || SRCDOC.equals(name) ||
            isAnUnsafeUrl(name, value)) {
          continue;
        }
        kept.add(name);
        kept.add(value);
      }
      return kept;
    }

    private boolean isAnUnsafeUrl(final String name, final String value) {
      if (!URL_ATTRIBUTES.contains(name)) {
        return false;
      }
      if (SCRIPTING_SCHEME.matcher(value).find()) {
        return true;
      }
      if (DATA_SCHEME.matcher(value).find() && !INLINED_IMAGE.matcher(value).find()) {
        return true;
      }
      // the poster of a video is an image, it obeys the very rule applied to the media
      return POSTER.equals(name) && isNotAllowedMedia(value);
    }

    private static String valueOf(final List<String> attributes) {
      for (int i = 0; i < attributes.size() - 1; i += 2) {
        if (SRC.equalsIgnoreCase(attributes.get(i))) {
          return attributes.get(i + 1);
        }
      }
      return null;
    }
  }
}
