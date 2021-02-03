/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.converter.openoffice.OpenOfficeService;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.io.media.image.imagemagick.Im4javaManager;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.util.SilverProperties;
import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.test.WarBuilder4Viewer;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractViewerIT {

  private static final String DOC_ID = "doc-id";
  private static final String LANG = "fr";

  static final DimensionOption IMG_PORTRAIT = DimensionOption.widthAndHeight(595, 842);
  static final DimensionOption IMG_LANDSCAPE = DimensionOption.widthAndHeight(612, 792);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Viewer.onWarForTestClass(AbstractViewerIT.class).build();
  }

  private static File tempPath;
  private static File resourceTestDir;
  private static List<Class<? extends Initialization>> services = Arrays.asList(
      Im4javaManager.class,
      SwfToolManager.class,
      JsonPdfToolManager.class,
      OpenOfficeService.class);

  private static void init() {
    if (tempPath == null) {
      SilverProperties properties =
          MavenTargetDirectoryRule.loadPropertiesForTestClass(AbstractViewerIT.class);
      properties.load("org/silverpeas/general.properties");
      tempPath = new File(properties.getProperty("tempPath"));
      resourceTestDir = MavenTargetDirectoryRule.getResourceTestDirFile(properties);
    }
  }

  protected static File getTemporaryPath() {
    return tempPath;
  }

  private static File getResourceTestDirFile() {
    return resourceTestDir;
  }

  @Before
  public void initCommonServices() throws Exception {
    init();
    for (Class<? extends Initialization> service: services) {
      Initialization serviceToInitialize = ServiceProvider.getService(service);
      serviceToInitialize.init();
    }
  }

  @After
  public void releaseCommonServices() throws Exception {
    for (Class<? extends Initialization> service: services) {
      Initialization serviceToInitialize = ServiceProvider.getService(service);
      serviceToInitialize.release();
    }
  }

  @SuppressWarnings("ConstantConditions")
  File getDocumentNamed(final String name) {
    return FileUtils.getFile(getResourceTestDirFile(), "org/silverpeas/viewer", name);
  }

  protected SimpleDocument getSimpleDocumentNamed(final String name) {
    final File document = getDocumentNamed(name);
    return new SimpleDocument(new SimpleDocumentPK("simple_doc_UUID_" + name, "instanceId"),
        "foreignId", 0, false,
        new SimpleAttachment(name, "fr", "title", "description", document.length(), "contentType",
            "me", new Date(), null)) {
      private static final long serialVersionUID = 4437882040649114634L;

      @Override
      public String getAttachmentPath() {
        return document.getPath();
      }
    };
  }

  boolean canPerformViewConversionTest() {
    if (SwfToolManager.isActivated()) {
      return true;
    }
    Logger.getAnonymousLogger().severe("SwfTools are not available, test is skipped.");
    Logger.getAnonymousLogger().severe("Please install pdf2swf and swfrender tools.");
    return false;
  }

  void saveInTemporaryPath(String simpleFileName, String value) throws Exception {
    FileUtils.writeStringToFile(new File(getTemporaryPath(), simpleFileName), value);
  }

  String readAndRemoveFromTemporaryPath(String simpleFileName) throws Exception {
    File fileToReadAndRemove = new File(getTemporaryPath(), simpleFileName);
    try {
      return FileUtils.readFileToString(fileToReadAndRemove);
    } finally {
      FileUtils.deleteQuietly(fileToReadAndRemove);
    }
  }

  Long readAndRemoveFromTemporaryPathAsLong(String simpleFileName) throws Exception {
    return Long.valueOf(readAndRemoveFromTemporaryPath(simpleFileName));
  }

  ViewerContext createViewerContext(final String originalFileName, final File originalSourceFile) {
    return new ViewerContext(DOC_ID, originalFileName, originalSourceFile, LANG);
  }

  void assertPreviewDimensions(final Preview preview, final DimensionOption... dimensions) {
    final List<Integer> widths = Stream.of(dimensions)
        .map(DimensionOption::getWidth)
        .collect(Collectors.toList());
    final List<Integer> heights = Stream.of(dimensions)
        .map(DimensionOption::getHeight)
        .collect(Collectors.toList());
    assertThat(widths.stream().anyMatch(w ->
        Matchers.closeTo(w, 1.0).matches(asDouble(preview.getWidth()))
    ), is(true));
    assertThat(heights.stream().anyMatch(h ->
        Matchers.closeTo(h, 1.0).matches(asDouble(preview.getHeight()))
    ), is(true));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(widths.stream().anyMatch(w ->
        Matchers.closeTo(w, 1.0).matches(asDouble(previewSize[0]))
    ), is(true));
    assertThat(heights.stream().anyMatch(h ->
        Matchers.closeTo(h, 1.0).matches(asDouble(previewSize[1]))
    ), is(true));
  }

  private static double asDouble(final String nb) {
    return Double.parseDouble(nb);
  }
}
