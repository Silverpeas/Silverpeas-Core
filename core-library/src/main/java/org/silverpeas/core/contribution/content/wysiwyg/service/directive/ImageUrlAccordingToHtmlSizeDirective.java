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

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective;
import org.silverpeas.core.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Character.isDigit;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * Transforms all URL of images to take into account theirs display size.
 * @author Yohann Chastagnier
 */
public class ImageUrlAccordingToHtmlSizeDirective implements WysiwygContentTransformerDirective {

  private static final String WIDTH_ATTR = "width";
  private static final String HEIGHT_ATTR = "height";
  private static final String STYLE_ATTR = "style";

  private static final List<RegexpPatternDirective> WIDTH_NUMERIC_VALUE = Arrays.asList(
      regexp(Pattern.compile("(?i)" + WIDTH_ATTR + "Attr[ ]*([0-9]+.?)"), 1),
      regexp(Pattern.compile("(?i)[ ;]" + WIDTH_ATTR + "[ ]*:[ ]*([0-9]+.?)"), 1));
  private static final List<RegexpPatternDirective> HEIGHT_NUMERIC_VALUE = Arrays.asList(
      regexp(Pattern.compile("(?i)" + HEIGHT_ATTR + "Attr[ ]*([0-9]+.?)"), 1),
      regexp(Pattern.compile("(?i)[ ;]" + HEIGHT_ATTR + "[ ]*:[ ]*([0-9]+.?)"), 1));

  private final int minWidth;

  public ImageUrlAccordingToHtmlSizeDirective() {
    this(null);
  }

  public ImageUrlAccordingToHtmlSizeDirective(final Integer minWidth) {
    this.minWidth = minWidth != null ? minWidth : 0;
  }

  @Override
  public String execute(final String wysiwygContent) {
    final String wysiwygToTransform = wysiwygContent != null ? wysiwygContent : "";
    final Source source = new Source(wysiwygToTransform);
    final List<Element> imgElements = source.getAllElements(HTMLElementName.IMG);
    final Map<String, String> replacements = new HashMap<>();
    if (!imgElements.isEmpty()) {
      final List<SrcTranslator> translators = SrcTranslator.getAll();
      for (Element currentImg : imgElements) {
        final String imgTagContent = currentImg.toString();
        final Attribute srcAtt = currentImg.getAttributes().get("src");
        if (srcAtt != null && srcAtt.getValueSegment() != null) {
          final String src = srcAtt.getValueSegment().toString();
          if (!replacements.containsKey(imgTagContent)) {
            final Mutable<String> width = Mutable.of(getWidth(currentImg));
            final Mutable<String> height = Mutable.of(getHeight(currentImg));
            applyMinWidthIfNecessary(width, height);
            translators.stream()
                .filter(s -> s.isCompliantUrl(src))
                .findFirst()
                .map(s -> s.translateUrl(src, width.get(), height.get()))
                .filter(t -> !src.equals(t))
                .ifPresent(t -> replacements.put(imgTagContent, imgTagContent.replace(src, t)));
          }
        }
      }
    }
    String transformedWysiwygContent = wysiwygToTransform;
    for (Map.Entry<String, String> replacement : replacements.entrySet()) {
      transformedWysiwygContent =
          transformedWysiwygContent.replace(replacement.getKey(), replacement.getValue());
    }

    // Returning the transformed WYSIWYG.
    return transformedWysiwygContent;
  }

  private void applyMinWidthIfNecessary(final Mutable<String> width, final Mutable<String> height) {
    Optional.of(minWidth)
        .filter(w -> w > 0)
        .map(o -> width.get())
        .filter(StringUtil::isDefined)
        .map(Integer::parseInt)
        .ifPresent(w -> {
          final BigDecimal ratio = new BigDecimal(w)
              .divide(new BigDecimal(minWidth), 10, RoundingMode.HALF_UP);
          if (ratio.floatValue() < 1f) {
            width.set(String.valueOf(minWidth));
            height.set(height
                .filter(StringUtil::isDefined)
                .map(BigDecimal::new)
                .map(h -> h.divide(ratio, 0, RoundingMode.HALF_UP))
                .map(BigDecimal::toString)
                .orElse(EMPTY));
          }
        });
  }

  /**
   * Gets the width, first from the width attribute if any, otherwise from style attribute.
   * @param imgElement image element.
   * @return the numeric width as string, empty string if no numeric value found.
   */
  private String getWidth(Element imgElement) {
    return getSizeOf(WIDTH_NUMERIC_VALUE, imgElement, WIDTH_ATTR);
  }

  /**
   * Gets the height, first from the height attribute if any, otherwise from style attribute.
   * @param imgElement image element.
   * @return the numeric height as string, empty string if no numeric value found.
   */
  private String getHeight(Element imgElement) {
    return getSizeOf(HEIGHT_NUMERIC_VALUE, imgElement, HEIGHT_ATTR);
  }

  /**
   * Centralized method.
   */
  private String getSizeOf(List<RegexpPatternDirective> directives, Element imgElement,
      String attrName) {
    String stringToParse =
        ";" + imgElement.getAttributeValue(STYLE_ATTR) + "@" + attrName + "Attr" +
            imgElement.getAttributeValue(attrName);
    List<String> sizeData =
        from(stringToParse.replaceAll("[\n\r]*", "")).withDirectives(directives).extract();
    return sizeData.stream()
        .filter(s -> s.charAt(s.length() - 1) != '%')
        .map(s -> isDigit(s.charAt(s.length() - 1)) ? s : s.substring(0, s.length() - 1))
        .findFirst()
        .orElse("");
  }

  /**
   * This interface permits to the different provider of images to translate an URL with given
   * height and width.
   * <p>
   * When {@link SrcTranslator} is called, the given MUST be an URL of an image.
   * Implementations are not in charge to verify this fact.
   * </p>
   */
  public interface SrcTranslator {

    static List<SrcTranslator> getAll() {
      final List<SrcTranslator> asList = ServiceProvider.getAllServices(SrcTranslator.class)
          .stream()
          .sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
          .collect(Collectors.toList());
      return Collections.unmodifiableList(asList);
    }

    /**
     * Indicates if the given URL is compliant with the current implementation.
     * @param url the URL to verify.
     * @return true if compliant, false otherwise.
     */
    boolean isCompliantUrl(final String url);

    /**
     * Translates the given URL to a new one which is taking into account the given width and the
     * given height.
     * <p>
     *   {@link #isCompliantUrl(String)} MUST be verified before calling this method.
     * </p>
     * @param url an URL as string.
     * @param width a width which could be an empty string to represent no width.
     * @param height a height which could be an empty string to represent no height.
     * @return the translated URL as string.
     */
    String translateUrl(final String url, final String width, final String height);
  }

  /**
   * Abstract implementation which centralizing the processing of an URL with parameters.
   */
  public abstract static class SrcWithSizeParametersTranslator implements SrcTranslator {

    private static final Pattern PATTERN = Pattern.compile("(?i)(&|&amp;)size[ ]*=[ ]*[0-9 x]+");
    private static final String AMP = "&amp;";

    @Override
    public String translateUrl(final String url, final String width, final String height) {
      // Computing the new src URL
      // at first, removing the size from the URL
      final Matcher matcher = PATTERN.matcher(url);
      final String existingPart = matcher.find() ? matcher.group() : EMPTY;
      String newUrl = url.replace(existingPart, EMPTY);
      // then guessing the new src URL
      StringBuilder sizeUrlPart = new StringBuilder().append(width).append("x").append(height);
      if (sizeUrlPart.length() > 1) {
        final String separator;
        if (url.indexOf('?') < 0) {
          separator = "?";
        } else {
          separator = url.contains(AMP) ? AMP : "&";
        }
        sizeUrlPart.insert(0, separator + "Size=");
        newUrl += sizeUrlPart;
      }
      return newUrl;
    }
  }
}
