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
import org.silverpeas.core.viewer.model.DocumentView;

import javax.inject.Inject;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class ViewServiceSplitMethodIT extends AbstractViewerIT {

  @Inject
  private ViewService viewService;

  @Inject
  private JsonPdfToolManager jsonPdfToolManager;

  @Before
  public void setup() {
    clearTemporaryPath();
    boolean isOk = getTemporaryPath().mkdirs();
    assertThat(isOk, is(true));

    setViewerSettings("org.silverpeas.viewer.viewer_with_split");
  }

  @After
  public void tearDown() {
    clearTemporaryPath();
  }

  @Test
  public void viewOdtFile() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(createViewerContext("file.pdf", getDocumentNamed("file.odt")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void viewOdtFileFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void viewPptFile() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(createViewerContext("file.pdf", getDocumentNamed("file.ppt")));
      assertPptDocumentView(view);
    }
  }

  @Test
  public void viewPptFileFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
      assertPptDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void viewPdfFile() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(createViewerContext("file.pdf", getDocumentNamed("file.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void viewPdfFileFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void viewPdfFileWithSpecialChars() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          createViewerContext("file ' - '' .pdf", getDocumentNamed("file ' - '' .pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void viewPdfFileWithSpecialCharsFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void viewPdfFileWithSpaces() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          createViewerContext("file with spaces.pdf", getDocumentNamed("file with spaces.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void viewPdfFileWithSpacesFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file with spaces.pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void viewPdfFileWithQuotes() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          createViewerContext("file_with_'_quotes_'.pdf",
              getDocumentNamed("file_with_'_quotes_'.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void viewPdfFileWithQuotesFromSimpleDocument() {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file_with_'_quotes_'.pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  private void assertPptDocumentView(DocumentView view) {
    assertDocumentView(view, 720, 540, false);
  }

  private void assertPptDocumentViewWithCacheManagement(DocumentView view) {
    assertDocumentView(view, 720, 540, true);
  }

  private void assertOfficeOrPdfDocumentView(DocumentView view) {
    assertDocumentView(view, 595, 842, false);
  }

  private void assertOfficeOrPdfDocumentViewWithCacheManagement(DocumentView view) {
    assertDocumentView(view, 595, 842, true);
  }

  @SuppressWarnings("ConstantConditions")
  private void assertDocumentView(DocumentView view, int width, int height,
      final boolean cacheUsed) {
    boolean jsonProcess = jsonPdfToolManager.isActivated();
    assertThat(view, notNullValue());
    int nbFilesAtTempRoot = cacheUsed ? 2 : 1;
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(nbFilesAtTempRoot));
    assertThat(view.getPhysicalFile().getName(), endsWith("page.swf"));
    assertThat(view.getWidth(), is(String.valueOf(width)));
    assertThat(view.getHeight(), is(String.valueOf(height)));
    assertThat(view.isDocumentSplit(), is(true));
    assertThat(view.areSearchDataComputed(), is(jsonProcess));

    // Files
    int nbPages = view.getNbPages();
    File[] files = view.getPhysicalFile().getParentFile().listFiles();
    int expectedNbTotalFiles = jsonProcess ? (nbPages + 1 + ((nbPages - 1) / 10)) : nbPages;
    assertThat(files.length, is(expectedNbTotalFiles));

    for (File file : files) {
      if (file.getName().startsWith("page.swf")) {
        assertThat(jsonProcess, is(true));
        assertThat(file.getName().replaceFirst("page[.]swf_[0-9]+[.]js", ""), is(emptyString()));
      } else if (file.getName().startsWith("page-")) {
        assertThat(file.getName().replaceFirst("page-[0-9]+.swf", ""), is(emptyString()));
      } else {
        fail("File '" + file.getPath() + "' is not expected");
      }
    }
  }
}
