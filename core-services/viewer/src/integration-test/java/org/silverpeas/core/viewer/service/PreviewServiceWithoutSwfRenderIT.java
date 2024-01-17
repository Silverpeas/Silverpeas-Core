/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.viewer.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.viewer.model.Preview;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PreviewServiceWithoutSwfRenderIT extends AbstractViewerIT {

  @Inject
  private PreviewService previewService;

  @Before
  public void setup() {
    clearTemporaryPath();
    boolean isOk = getTemporaryPath().mkdirs();
    assertThat(isOk, is(true));
  }

  @After
  public void tearDown() {
    clearTemporaryPath();
  }

  @Test
  public void previewOdtFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.odt", getDocumentNamed("file.odt")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewOdtFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewDocFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.doc", getDocumentNamed("file.doc")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewDocFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.doc")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewDocxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.docx", getDocumentNamed("file.docx")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewDocxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.docx")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewDocxFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .docx", getDocumentNamed("file ' - '' .docx")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewDocxFileWithSpecialCharsFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .docx")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewOdpFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.odp", getDocumentNamed("file.odp")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void previewOdpFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewPptFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.ppt", getDocumentNamed("file.ppt")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void previewPptFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewPptxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.pptx", getDocumentNamed("file.pptx")));
    assertPptDocumentPreview(preview);
  }

  @Test
  public void previewPptxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.pptx")));
    assertPptDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewOdsFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.ods", getDocumentNamed("file.ods")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewOdsFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.ods")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewXlsFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.xls", getDocumentNamed("file.xls")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewXlsFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.xls")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewXlsxFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.xlsx", getDocumentNamed("file.xlsx")));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName(), startsWith("file."));
  }

  @Test
  public void previewXlsxFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.xlsx")));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName(), startsWith("file."));
  }

  @Test
  public void previewJpgFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.jpg", getDocumentNamed("file.jpg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void previewJpgFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.jpg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewJpgFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .jpg", getDocumentNamed("file ' - '' .jpg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void previewJpgFileWithSpecialCharsFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .jpg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewJpegFile() {
    // The uppercase letter of the following file extension is not a mistake
    final Preview preview =
        previewService.getPreview(createViewerContext("file.jpEg", getDocumentNamed("file.jpEg")));
    assertImageDocumentPreview(preview);
  }

  @Test
  public void previewJpegFileFromSimpleDocument() {
    // The uppercase letter of the following file extension is not a mistake
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.jpEg")));
    assertImageDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewPdfFile() {
    final Preview preview =
        previewService.getPreview(createViewerContext("file.pdf", getDocumentNamed("file.pdf")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewPdfFileFromSimpleDocument() {
    final Preview preview =
        previewService.getPreview(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
    assertOfficeOrPdfDocumentPreviewWithCacheManagement(preview);
  }

  @Test
  public void previewPdfFileWithSpecialChars() {
    final Preview preview = previewService
        .getPreview(createViewerContext("file ' - '' .pdf", getDocumentNamed("file ' - '' .pdf")));
    assertOfficeOrPdfDocumentPreview(preview);
  }

  @Test
  public void previewPdfFileWithSpecialCharsFromSimpleDocument() {
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
    final int nbFilesAtTempRoot = cacheUsed ? 2 : 1;
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(nbFilesAtTempRoot));
    assertThat(preview.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(preview.getPhysicalFile().getName(), startsWith("file."));
    assertPreviewDimensions(preview, dimensions);
  }
}
