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
package org.silverpeas.core.contribution.content.wysiwyg.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.annotation.Bean;
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
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import jakarta.activation.FileDataSource;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

@EnableSilverTestEnv(context = JEETestContext.class)
public class WysiwygContentTransformerTest {

  private static final String ODT_NAME = "LibreOffice.odt";
  private static final String IMAGE_NAME = "image-test.jpg";

  private File originalOdt;
  private File originalImage;
  private List<File> filesWithResize;

  private static final String ODT_ATTACHMENT_ID = "72f56ba9-b089-40c4-b16c-255e93658259";

  @RegisterExtension
  protected SettingBundleStub urlSettings = new SettingBundleStub("org.silverpeas.wysiwyg.settings.wysiwygSettings");

  @RegisterExtension
  static SettingBundleStub mailSettings = new SettingBundleStub("org.silverpeas.mail.mail");

  @RegisterExtension
  static SettingBundleStub securitySettings =
      new SettingBundleStub("org.silverpeas.util.security");

  @TestManagedMock
  private AttachmentService attachmentService;

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
    urlSettings.put("mail.mime.multipart", "relative");
    filesWithResize = new ArrayList<>();
    mailSettings.put("image.resize.min-width", "0");
    securitySettings.put("security.external.iframe.hosts.allowed", "www.youtube.com");
    securitySettings.put("security.external.media.hosts.allowed", "www.youtube.com");
    originalOdt = new File(Objects.requireNonNull(getClass().getResource("/" + ODT_NAME)).getPath());
    assertThat(originalOdt.exists(), is(true));
    originalImage = new File(Objects.requireNonNull(getClass().getResource("/" + IMAGE_NAME)).getPath());
    assertThat(originalImage.exists(), is(true));
    addFileIntoContextWithWidth(100);

    // SilverpeasFile
    List<SilverpeasFileProcessor> processors = (List<SilverpeasFileProcessor>) FieldUtils
        .readDeclaredStaticField(SilverpeasFileProvider.class, "processors", true);
    processors.clear();
    SilverpeasFileProvider.addProcessor(new AttachmentUrlLinkProcessor());

    /*
    Mocking methods of attachment service instance
     */

    // searchDocumentById returns always a simple document which the PK is the one specified
    // from method parameters.
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
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

  private void addFileIntoContextWithWidth(int width) throws IOException {
    File originalImageWithWidthResizing = new File(originalImage.getParentFile(),
        width + "x/" + IMAGE_NAME);
    filesWithResize.add(originalImageWithWidthResizing);
    FileUtils.touch(originalImageWithWidthResizing);
    assertThat(originalImageWithWidthResizing.exists(), is(true));
    File originalImageWithResizeWidthAndHeightResizing = new File(originalImage.getParentFile(),
        width + "x" + width + "/" + IMAGE_NAME);
    filesWithResize.add(originalImageWithResizeWidthAndHeightResizing);
    FileUtils.touch(originalImageWithResizeWidthAndHeightResizing);
    assertThat(originalImageWithResizeWidthAndHeightResizing.exists(), is(true));
  }

  @SuppressWarnings("unchecked")
  @AfterEach
  void destroy() throws Exception {
    filesWithResize.forEach(FileUtils::deleteQuietly);

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
        "wysiwygWithSeveralTypesOfLinkTransformedForMailSendingResultWithImageResizePreProcessing.txt")));
  }

  @Test
  void toMailContentWithMinimalWidthForImages() throws Exception {
    addFileIntoContextWithWidth(400);
    mailSettings.put("image.resize.min-width", "400");
    WysiwygContentTransformer transformer = WysiwygContentTransformer
        .on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    MailContentProcess.MailResult mailResult = transformer.toMailContent();

    assertThat(mailResult.getWysiwygContent(), is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForMailSendingResultWithImageResizePreProcessingWithImageMinimalWidth.txt")));
  }

  @Test
  void manageImageResizing() {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralImages.txt"));

    String result = transformer.modifyImageUrlAccordingToHtmlSizeDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralImagesTransformedForImageResizingResult.txt")));
  }

  @Test
  void applyingSilverpeasLinkCss() {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    String result = transformer.applySilverpeasLinkCssDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForCssLinkApplierResult.txt")));
  }

  @Test
  void applyingSilverpeasBlankLinks() {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    String result = transformer.applyOpenLinkOnBlankDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForOpeningOnBlankPage.txt")));
  }

  @Test
  void sanitizeFromHtml() {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithFullHtml.txt"));

    String result = transformer.applySanitizeDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithFullHtmlTransformedBySanitization.txt")));
  }

  @Test
  void sanitizeForRenderingDropsWhatCanActOnTheBrowser() {
    assertThat(sanitizedForRendering("<script>alert(1)</script><p>text</p>"), is("<p>text</p>"));
    assertThat(sanitizedForRendering("<img src=\"/silverpeas/x.png\" onerror=\"alert(1)\" />"),
        is("<img src=\"/silverpeas/x.png\" />"));
    // the event callback isn't preceded by a whitespace, which declares it all the same
    assertThat(sanitizedForRendering("<img src=\"/silverpeas/x.png\"onerror=alert(1) />"),
        is("<img src=\"/silverpeas/x.png\" />"));
    assertThat(sanitizedForRendering("<a href=\"javascript:alert(1)\">a link</a>"),
        is("<a>a link</a>"));
    // the HTML entities are decoded by the tokenizer before the scheme is looked at
    assertThat(sanitizedForRendering("<a href=\"&#106;avascript:alert(1)\">a link</a>"),
        is("<a>a link</a>"));
    // the element is dropped along with its content, but not with what follows it
    assertThat(sanitizedForRendering("<svg onload=\"alert(1)\"><circle/></svg>after"), is("after"));
    assertThat(sanitizedForRendering("<object><param name=\"a\"></object>after"), is("after"));
  }

  /**
   * The WYSIWYG editor of Silverpeas is set up to accept any content, so the rendering must not
   * apply an allow list narrower than what the users are entitled to write. In particular, the
   * media its own plugins produce and the attributes its own scripts read have to go through.
   */
  @Test
  void sanitizeForRenderingKeepsTheContentAsItIs() {
    assertThat(sanitizedForRendering(
            "<video controls><source src=\"/silverpeas/v.mp4\" type=\"video/mp4\"></video>"),
        is("<video controls=\"controls\"><source src=\"/silverpeas/v.mp4\" type=\"video/mp4\" />" +
            "</video>"));
    assertThat(sanitizedForRendering("<audio controls src=\"/silverpeas/a.mp3\"></audio>"),
        is("<audio controls=\"controls\" src=\"/silverpeas/a.mp3\"></audio>"));
    assertThat(sanitizedForRendering("<pre><code class=\"language-java\">int i;</code></pre>"),
        is("<pre><code class=\"language-java\">int i;</code></pre>"));
    assertThat(sanitizedForRendering("<figure><figcaption>a caption</figcaption></figure>"),
        is("<figure><figcaption>a caption</figcaption></figure>"));
    // the userzoom and identitycard plugins carry the user id by the rel attribute, which the
    // silverpeas-userZoom script reads back
    assertThat(sanitizedForRendering("<span class=\"userToZoom\" rel=\"42\">John</span>"),
        is("<span class=\"userToZoom\" rel=\"42\">John</span>"));
    assertThat(sanitizedForRendering("<p data-sp-id=\"7\" style=\"color:red\">text</p>"),
        is("<p data-sp-id=\"7\" style=\"color:red\">text</p>"));
    assertThat(
        sanitizedForRendering("<a href=\"https://www.silverpeas.org\" target=\"_blank\">a link</a>"),
        is("<a href=\"https://www.silverpeas.org\" target=\"_blank\">a link</a>"));
  }

  /**
   * The media obey the very rule applied to the iframes: those Silverpeas hosts itself are always
   * allowed, the external ones have to be declared. An inlined image is carried by the content
   * itself and is therefore kept.
   */
  @Test
  void sanitizeForRenderingKeepsOnlyTheMediaReferringAnAllowedSource() {
    assertThat(sanitizedForRendering("<img src=\"/silverpeas/x.png\" />"),
        is("<img src=\"/silverpeas/x.png\" />"));
    assertThat(sanitizedForRendering("<img src=\"https://www.unallowed.org/x.png\" />"), is(""));
    assertThat(sanitizedForRendering("<img src=\"https://www.youtube.com/x.png\" />"),
        is("<img src=\"https://www.youtube.com/x.png\" />"));
    // the renderer escapes the equal signs of the attribute values, which the browsers decode back
    assertThat(sanitizedForRendering("<img src=\"data:image/png;base64,iVBORw0KGgo=\" />"),
        is("<img src=\"data:image/png;base64,iVBORw0KGgo&#61;\" />"));
    assertThat(sanitizedForRendering("<img src=\"data:text/html;base64,PHNjcmlwdD4=\" />"), is(""));
    assertThat(sanitizedForRendering(
            "<video src=\"https://www.unallowed.org/v.mp4\">a fallback</video>"), is(""));
    // the poster of a video is an image, only the attribute is dropped
    assertThat(sanitizedForRendering(
            "<video src=\"/silverpeas/v.mp4\" poster=\"https://www.unallowed.org/p.png\"></video>"),
        is("<video src=\"/silverpeas/v.mp4\"></video>"));
  }

  /**
   * Whereas the default sanitization, aimed at the contents extracted out of Silverpeas, keeps
   * strictly the safe content.
   */
  @Test
  void defaultSanitizeDropsThePresentationAttributes() {
    String result = WysiwygContentTransformer.on("<div class=\"aClass\" id=\"anId\">text</div>")
        .applySanitizeDirective()
        .transform();
    assertThat(result, is("<div>text</div>"));
  }

  /**
   * The rendering keeps the iframes Silverpeas accepts at the very moment a content embedding them
   * is submitted, so that sanitizing a content doesn't drop what has been legitimately stored.
   */
  @Test
  void sanitizeForRenderingKeepsOnlyTheIFramesReferringAnAllowedSource() {
    assertThat(sanitizedForRendering("<iframe src=\"https://www.youtube.com/embed/xyz\"></iframe>"),
        is("<iframe src=\"https://www.youtube.com/embed/xyz\"></iframe>"));
    assertThat(sanitizedForRendering("<iframe src=\"/silverpeas/Rkmelia/kmelia1/Main\"></iframe>"),
        is("<iframe src=\"/silverpeas/Rkmelia/kmelia1/Main\"></iframe>"));
    assertThat(sanitizedForRendering("<iframe src=\"https://www.evil.org/\"></iframe>"), is(""));
    assertThat(sanitizedForRendering("<iframe src=\"http://www.youtube.com/embed/xyz\"></iframe>"),
        is(""));
    assertThat(sanitizedForRendering("<iframe src=\"/other/app\"></iframe>"), is(""));
    assertThat(sanitizedForRendering("<iframe src=\"/silverpeas/../other/app\"></iframe>"), is(""));
    assertThat(sanitizedForRendering("<iframe src=\"javascript:alert(1)\"></iframe>"), is(""));
    assertThat(sanitizedForRendering("<iframe></iframe>"), is(""));
    assertThat(
        sanitizedForRendering("<iframe srcdoc=\"&lt;script&gt;alert(1)&lt;/script&gt;\"></iframe>"),
        is(""));
  }

  private String sanitizedForRendering(final String content) {
    return WysiwygContentTransformer.on(content).applySanitizeForRenderingDirective().transform();
  }

  @Test
  void sanitizeFromSimpleText() {
    WysiwygContentTransformer transformer = WysiwygContentTransformer.on("Silverpeas's < simple\nText\t or > toto");

    String result = transformer.applySanitizeDirective().transform();

    assertThat(result, is("Silverpeas&#39;s &lt; simple\nText\t or &gt; toto"));
  }

  @Test
  void applyingMailLinkCss() {
    WysiwygContentTransformer transformer =
        WysiwygContentTransformer.on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    String result = transformer.applyMailLinkCssDirective().transform();

    assertThat(result, is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForMailCssLinkResult.txt")));
  }

  /*
  TOOL METHODS
   */

  private synchronized static String getContentOfDocumentNamed(final String name) {
    try {
      return FileUtil.readFileToString(Objects.requireNonNull(getDocumentNamed(name)));
    } catch (IOException e) {
      return null;
    }
  }

  private synchronized static File getDocumentNamed(final String name) {
    final URL documentLocation = WysiwygContentTransformerTest.class.getResource(name);
    try {
      return new File(Objects.requireNonNull(documentLocation).toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  @Bean
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

  @Bean
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