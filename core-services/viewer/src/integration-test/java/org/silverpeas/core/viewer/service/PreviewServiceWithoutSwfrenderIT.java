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
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.model.ViewerSettings;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class PreviewServiceWithoutSwfrenderIT extends AbstractViewerIT {
  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Inject
  private PreviewService previewService;

  @Before
  public void setup() {
    FileUtils.deleteQuietly(getTemporaryPath());
    //noinspection ResultOfMethodCallIgnored
    getTemporaryPath().mkdirs();
    final SettingBundle mockedSettings =
        reflectionRule.mockField(ViewerSettings.class, SettingBundle.class, "settings");
    when(mockedSettings.getInteger(eq("preview.width.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getInteger(eq("preview.height.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getBoolean(eq("viewer.cache.enabled"), anyBoolean())).thenReturn(true);
    when(mockedSettings.getBoolean(eq("viewer.cache.conversion.silent.enabled"), anyBoolean()))
        .thenReturn(false);
    when(mockedSettings.getBoolean(eq("viewer.conversion.strategy.split.enabled"), anyBoolean()))
        .thenReturn(false);
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(getTemporaryPath());
  }

  @Test
  public void testOdtFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.odt", getDocumentNamed("file.odt")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testOdtFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testDocFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.doc", getDocumentNamed("file.doc")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testDocFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.doc")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testDocxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.docx", getDocumentNamed("file.docx")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testDocxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.docx")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testDocxFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .docx", getDocumentNamed("file ' - '' .docx")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testDocxFileWithSpecialCharsFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .docx")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testOdpFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.odp", getDocumentNamed("file.odp")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void testOdpFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testPptFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.ppt", getDocumentNamed("file.ppt")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void testPptFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testPptxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.pptx", getDocumentNamed("file.pptx")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void testPptxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.pptx")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testOdsFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.ods", getDocumentNamed("file.ods")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testOdsFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.ods")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testXlsFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.xls", getDocumentNamed("file.xls")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testXlsFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.xls")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testXlsxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.xlsx", getDocumentNamed("file.xlsx")));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName(), startsWith("file."));
//    The following assertions are comented out as the result size depends on the OpenOffice version
//    (OpenOffice.org or LibreOffice)
//    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
//    assertThat(previewSize[0], is("595"));
//    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testXlsxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.xlsx")));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName(), startsWith("file."));
//    The following assertions are comented out as the result size depends on the OpenOffice version
//    (OpenOffice.org or LibreOffice)
//    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
//    assertThat(previewSize[0], is("595"));
//    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testJpgFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.jpg", getDocumentNamed("file.jpg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void testJpgFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.jpg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testJpgFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .jpg", getDocumentNamed("file ' - '' .jpg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void testJpgFileWithSpecialCharsFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .jpg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testJpegFile() {
    // The uppercase letter of the following file extension is not a mistake
    final Preview preview =
        previewService.getPreview(createViewerContext("file.jpEg", getDocumentNamed("file.jpEg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void testJpegFileFromSimpleDocument() {
    // The uppercase letter of the following file extension is not a mistake
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.jpEg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testPdfFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.pdf", getDocumentNamed("file.pdf")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testPdfFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void testPdfFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .pdf", getDocumentNamed("file ' - '' .pdf")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void testPdfFileWithSpecialCharsFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .pdf")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  private void assertPptDocumentPreview(Preview preview) {
    assertDocumentPreview(preview, false, DimensionOption.widthAndHeight(720, 540));
  }

  private void assertPptDocumentPreviewWithCacheManagement(Preview preview) {
    assertDocumentPreview(preview, true, DimensionOption.widthAndHeight(720, 540));
  }

  private void assertImageDocumentPreview(Preview preview) {
    assertDocumentPreview(preview, false, DimensionOption.widthAndHeight(1000, 750));
  }

  private void assertImageDocumentPreviewWithCacheManagement(Preview preview) {
    assertDocumentPreview(preview, true, DimensionOption.widthAndHeight(1000, 750));
  }

  private void assertOfficeOrPdfDocumentPreview(Preview preview) {
    assertDocumentPreview(preview, false, IMG_PORTRAIT, IMG_LANDSCAPE);
  }

  private void assertOfficeOrPdfDocumentPreviewWithCacheManagement(Preview preview) {
    assertDocumentPreview(preview, true, IMG_PORTRAIT, IMG_LANDSCAPE);
  }

  private void assertDocumentPreview(Preview preview, final boolean cacheUsed,
      final DimensionOption... dimensions) {
    assertThat(preview, notNullValue());
    final String fileExtension = "jpg";
    final int nbFilesAtTempRoot = cacheUsed ? 2 : 1;
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(nbFilesAtTempRoot));
    assertThat(preview.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(preview.getPhysicalFile().getName(), is("file." + fileExtension));
    assertPreviewDimensions(preview, dimensions);
  }
}
