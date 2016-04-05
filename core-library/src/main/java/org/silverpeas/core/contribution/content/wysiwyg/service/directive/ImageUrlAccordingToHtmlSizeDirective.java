/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;

/**
 * Transforms all URL of images to take into account theirs display size.
 * @author Yohann Chastagnier
 */
public class ImageUrlAccordingToHtmlSizeDirective implements WysiwygContentTransformerDirective {

  private static final String WIDTH_ATTR = "width";
  private static final String HEIGHT_ATTR = "height";
  private static final String STYLE_ATTR = "style";

  private static List<RegexpPatternDirective> WIDTH_NUMERIC_VALUE = Arrays
      .asList(regexp(Pattern.compile("(?i)" + WIDTH_ATTR + "Attr[ ]*([0-9]+)"), 1),
          regexp(Pattern.compile("(?i)[ ;]" + WIDTH_ATTR + "[ ]*:[ ]*([0-9]+)"), 1));
  private static List<RegexpPatternDirective> HEIGHT_NUMERIC_VALUE = Arrays
      .asList(regexp(Pattern.compile("(?i)" + HEIGHT_ATTR + "Attr[ ]*([0-9]+)"), 1),
          regexp(Pattern.compile("(?i)[ ;]" + HEIGHT_ATTR + "[ ]*:[ ]*([0-9]+)"), 1));

  @Override
  public String execute(final String wysiwygContent) {
    String wysiwygToTransform = wysiwygContent != null ? wysiwygContent : "";
    Source source = new Source(wysiwygToTransform);
    List<Element> imgElements = source.getAllElements(HTMLElementName.IMG);
    Map<String, String> replacements = new HashMap<String, String>();
    for (Element currentImg : imgElements) {

      // The part that is not modified
      String imgTagContent = currentImg.toString();

      String src = currentImg.getAttributeValue("src");
      if (src.contains("/attachmentId/") && !replacements.containsKey(imgTagContent)) {

        // Computing the new src URL
        // at first, removing the size from the URL
        String newSrc = src.replaceFirst("(?i)/size/[0-9 x]+", "");

        // then guessing the new src URL
        String width = getWidth(currentImg);
        String height = getHeight(currentImg);
        String sizeUrlPart = width + "x" + height;
        if (sizeUrlPart.length() > 1) {
          sizeUrlPart = "/size/" + sizeUrlPart;
          newSrc = newSrc.replaceFirst("/name/", sizeUrlPart + "/name/");
        }

        // Applying the new URL if necessary
        if (!src.equals(newSrc)) {
          replacements.put(imgTagContent, imgTagContent.replace(src, newSrc));
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
    return sizeData.isEmpty() ? "" : sizeData.get(0);
  }
}
