/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.contribution.content.wysiwyg.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.SimpleDocumentUrlAccordingToHtmlSizeDirectiveTranslator;
import org.silverpeas.core.contribution.attachment.SimpleDocumentUrlToDataSourceScanner;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.contribution.content.wysiwyg.service.directive.ImageUrlAccordingToHtmlSizeDirective;
import org.silverpeas.core.contribution.content.wysiwyg.service.process.MailContentProcess;
import org.silverpeas.core.html.PermalinkRegistry;
import org.silverpeas.core.io.file.AttachmentUrlLinkProcessor;
import org.silverpeas.core.io.file.SilverpeasFileProcessor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUtil;

import javax.activation.FileDataSource;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

@EnableSilverTestEnv
public class WysiwygContentTransformerTest {

  private static final String ODT_NAME = "LibreOffice.odt";
  private static final String IMAGE_NAME = "image-test.jpg";

  private File originalOdt;
  private File originalImage;
  private File originalImageWithResize100x;
  private File originalImageWithResize100x100;

  private static final String ODT_ATTACHMENT_ID = "72f56ba9-b089-40c4-b16c-255e93658259";

  @TestManagedBean
  private MailContentProcess.WysiwygCkeditorMediaLinkUrlToDataSourceScanner ckScanner;

  @TestManagedBean
  private SimpleDocumentUrlToDataSourceScanner attScanner;

  @TestManagedBean
  private SimpleDocumentUrlAccordingToHtmlSizeDirectiveTranslator attSrcTranslator;

  @TestManagedBean
  private GalleryLinkUrlDataSourceScanner4Test gallScanner;

  @TestManagedBean
  private GalleryImageUrlAccordingToHtmlSizeDirectiveTranslator4Test gallTranslator;

  @TestManagedBean
  private PermalinkRegistry permalinkRegistry;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() throws Exception {
    originalOdt = new File(getClass().getResource("/" + ODT_NAME).getPath());
    assertThat(originalOdt.exists(), is(true));
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    originalImageWithResize100x = new File(originalImage.getParentFile(), "100x/" + IMAGE_NAME);
    FileUtils.touch(originalImageWithResize100x);
    assertThat(originalImageWithResize100x.exists(), is(true));
    originalImageWithResize100x100 =
        new File(originalImage.getParentFile(), "100x100/" + IMAGE_NAME);
    FileUtils.touch(originalImageWithResize100x100);
    assertThat(originalImageWithResize100x100.exists(), is(true));

    // SilverpeasFile
    List<SilverpeasFileProcessor> processors = (List<SilverpeasFileProcessor>) FieldUtils
        .readDeclaredStaticField(SilverpeasFileProvider.class, "processors", true);
    processors.clear();
    SilverpeasFileProvider.addProcessor(new AttachmentUrlLinkProcessor());

    // The mock instance
    AttachmentService mockAttachmentService = mock(AttachmentService.class);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(AttachmentService.class))
        .thenReturn(mockAttachmentService);

    /*
    Mocking methods of attachment service instance
     */

    // searchDocumentById returns always a simple document which the PK is the one specified
    // from method parameters.
    when(mockAttachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
        .then(invocation -> {
          SimpleDocumentPK pk = (SimpleDocumentPK) invocation.getArguments()[0];
          SimpleDocument simpleDocument = mock(SimpleDocument.class);
          when(simpleDocument.getPk()).thenReturn(pk);
          if (pk.getId().contains(ODT_ATTACHMENT_ID)) {
            when(simpleDocument.getAttachmentPath()).thenReturn(originalOdt.getPath());
          } else {
            when(simpleDocument.getAttachmentPath()).thenReturn(originalImage.getPath());
          }
          return simpleDocument;
        });

    /*
    Setting the server start URL
     */
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    when(mockHttpServletRequest.getScheme()).thenReturn("http");
    when(mockHttpServletRequest.getServerName()).thenReturn("www.unit-test-silverpeas.org");
    when(mockHttpServletRequest.getServerPort()).thenReturn(80);
    URLUtil.setCurrentServerUrl(mockHttpServletRequest);
  }

  @SuppressWarnings("unchecked")
  @AfterEach
  void destroy() throws Exception {
    FileUtils.deleteQuietly(originalImageWithResize100x.getParentFile());
    FileUtils.deleteQuietly(originalImageWithResize100x100.getParentFile());

    // SilverpeasFile
    List<SilverpeasFileProcessor> processors = (List<SilverpeasFileProcessor>) FieldUtils
        .readDeclaredStaticField(SilverpeasFileProvider.class, "processors", true);
    processors.clear();
  }

  @Test
  void toMailContent() throws Exception {
    WysiwygContentTransformer transformer = WysiwygContentTransformer
        .on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    MailContentProcess.MailResult mailResult = transformer.toMailContent();

    assertThat(mailResult.getWysiwygContent(), is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForMailSendingResultWithImageResizePreProcessing" +
            ".txt")));
  }

  @Test
  void manageImageResizing() throws Exception {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralImages.txt"));

    String result = transformer.modifyImageUrlAccordingToHtmlSizeDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralImagesTransformedForImageResizingResult.txt")));
  }

  @Test
  public void applyingSilverpeasLinkCss() throws Exception {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    String result = transformer.applySilverpeasLinkCssDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForCssLinkApplierResult.txt")));
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
    final URL documentLocation = WysiwygContentTransformerTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  @Singleton
  public static class GalleryLinkUrlDataSourceScanner4Test implements LinkUrlDataSourceScanner {

    private static final Pattern GALLERY_CONTENT_LINK_PATTERN = Pattern.compile("(?i)=\"([^\"]*/GalleryInWysiwyg/[^\"]+)");

    @Override
    public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
      final List<LinkUrlDataSource> result = new ArrayList<>();
      from(htmlContent).withDirectives(singletonList(regexp(GALLERY_CONTENT_LINK_PATTERN, 1))).extract().forEach(l -> {
        final File imageFile = mock(File.class);
        when(imageFile.exists()).thenReturn(true);
        when(imageFile.getPath()).thenReturn("image.jpg");
        result.add(new LinkUrlDataSource(l, () -> new FileDataSource(imageFile)));
      });
      return result;
    }
  }

  @Singleton
  public static class GalleryImageUrlAccordingToHtmlSizeDirectiveTranslator4Test
      implements ImageUrlAccordingToHtmlSizeDirective.SrcTranslator {

    @Override
    public boolean isCompliantUrl(final String url) {
      return defaultStringIfNotDefined(url).contains("/GalleryInWysiwyg/");
    }

    @Override
    public String translateUrl(final String url, final String width, final String height) {
      return url + "&amp;Size=TEST";
    }
  }
}