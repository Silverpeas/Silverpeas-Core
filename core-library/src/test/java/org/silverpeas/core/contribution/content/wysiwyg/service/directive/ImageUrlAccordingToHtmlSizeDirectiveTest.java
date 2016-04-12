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

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.contribution.content.wysiwyg.service.TestWysiwygContentTransformer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@BenchmarkOptions(benchmarkRounds = 500, warmupRounds = 500)
public class ImageUrlAccordingToHtmlSizeDirectiveTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public BenchmarkRule benchmarkRule = new BenchmarkRule();

  @Test
  public void manageImageResizing() throws Exception {
    ImageUrlAccordingToHtmlSizeDirective directive = new ImageUrlAccordingToHtmlSizeDirective();
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithSeveralImages.txt");

    String result = directive.execute(wysiwygContentSource);

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralImagesTransformedForImageResizingResult.txt")));
  }

  @Test
  public void manageImageResizingOnEmptyContent() throws Exception {
    ImageUrlAccordingToHtmlSizeDirective directive = new ImageUrlAccordingToHtmlSizeDirective();
    String result = directive.execute("");
    assertThat(result, is(""));
  }

  @Test
  public void manageImageResizingOnNullContent() throws Exception {
    ImageUrlAccordingToHtmlSizeDirective directive = new ImageUrlAccordingToHtmlSizeDirective();
    String result = directive.execute(null);
    assertThat(result, is(""));
  }

  @Test
  public void manageImageResizingButNoImageToResize() throws Exception {
    ImageUrlAccordingToHtmlSizeDirective directive = new ImageUrlAccordingToHtmlSizeDirective();
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithoutAttachmentImages.txt");

    String result = directive.execute(wysiwygContentSource);

    assertThat(result, is(getContentOfDocumentNamed("wysiwygWithoutAttachmentImages.txt")));
  }

  @Test
  public void imageResizingPerformanceWithCurrentImpl() throws Exception {
    ImageUrlAccordingToHtmlSizeDirective directive = new ImageUrlAccordingToHtmlSizeDirective();
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithSeveralImages.txt");
    directive.execute(wysiwygContentSource);
  }

  @Test
  public void oldImageResizingPerformance() throws Exception {
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithSeveralImages.txt");
    updateURLOfImagesAccordingToSizes(wysiwygContentSource);
  }

  /**
   * That was the precedent implementation (it contains some code error, but the aim here is to
   * compare the performances).
   */
  public String updateURLOfImagesAccordingToSizes(String transformedWysiwygContent) {
    Source source = new Source(transformedWysiwygContent);
    //get all images
    List<Element> images = source.getAllElements(HTMLElementName.IMG);
    Map<String, String> replacements = new HashMap<>();
    for (Element image : images) {
      String src = image.getAttributeValue("src");

      //update only URL of images stored in Silverpeas
      if (src.contains("/attachmentId/")) {
        String width = image.getAttributeValue("width");

        // extract width from 'style' attribute
        if (!StringUtil.isDefined(width)) {
          String style = image.getAttributeValue("style");
          if (StringUtil.isDefined(style)) {
            int i = style.indexOf("width:");
            if (i != -1) {
              int j = style.indexOf(";", i);
              if (j != -1) {
                width = style.substring(i + 6, j).trim();
              }
            }
          }
        }

        // process URL
        String newSrc;
        if (StringUtil.isDefined(width)) {
          if (width.contains("px")) {
            //remove 'px'
            width = width.substring(0, width.length() - 2);
          }

          if (src.contains("/size/")) {
            //replace existing 'size' parameter
            int i = src.indexOf("/size/");
            int j = src.indexOf("/", i + "/size/".length());
            String size = src.substring(i, j);
            newSrc = src.replace(size, "/size/" + width + "x");
            replacements.put(src, newSrc);
          } else {
            //add 'size' parameter
            int i = src.indexOf("/name/");
            if (i != -1) {
              newSrc = src.substring(0, i);
              newSrc += "/size/" + width + "x";
              newSrc += src.substring(i, src.length());
              replacements.put(src, newSrc);
            }
          }
        }
      }

      //replace all Silverpeas images URL
      for (String url : replacements.keySet()) {
        transformedWysiwygContent =
            transformedWysiwygContent.replaceAll(url, replacements.get(url));
      }
    }

    return transformedWysiwygContent;
  }


  /*
  TOOL METHODS
   */

  private synchronized static String getContentOfDocumentNamed(final String name) {
    try {
      return FileUtil.readFileToString(getDocumentNamed(name));
    } catch (IOException e) {
      return null;
    }
  }

  private synchronized static File getDocumentNamed(final String name) {
    final URL documentLocation = TestWysiwygContentTransformer.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}