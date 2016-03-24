package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.model.ViewerSettings;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class ViewServiceTest extends AbstractViewerTest {

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Inject
  private ViewService viewService;

  @Before
  public void setup() throws Exception {
    FileUtils.deleteQuietly(getTemporaryPath());
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
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(getTemporaryPath());
  }

  @Test
  public void testOdtFileView() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(new ViewerContext("file.pdf", getDocumentNamed("file.odt")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void testOdtFileViewFromSimpleDocument() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void testPptFileView() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(new ViewerContext("file.pdf", getDocumentNamed("file.ppt")));
      assertPptDocumentView(view);
    }
  }

  @Test
  public void testPptFileViewFromSimpleDocument() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
      assertPptDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void testPdfFileView() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(new ViewerContext("file.pdf", getDocumentNamed("file.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void testPdfFileViewFromSimpleDocument() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void testPdfFileViewWithSpecialChars() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          new ViewerContext("file ' - '' .pdf", getDocumentNamed("file ' - '' .pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void testPdfFileViewWithSpecialCharsFromSimpleDocument() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file ' - '' .pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void testPdfFileViewWithSpaces() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          new ViewerContext("file with spaces.pdf", getDocumentNamed("file with spaces.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void testPdfFileViewWithSpacesFromSimpleDocument() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService
          .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file with spaces.pdf")));
      assertOfficeOrPdfDocumentViewWithCacheManagement(view);
    }
  }

  @Test
  public void testPdfFileViewWithQuotes() throws Exception {
    if (canPerformViewConversionTest()) {
      final DocumentView view = viewService.getDocumentView(
          new ViewerContext("file_with_'_quotes_'.pdf",
              getDocumentNamed("file_with_'_quotes_'.pdf")));
      assertOfficeOrPdfDocumentView(view);
    }
  }

  @Test
  public void testPdfFileViewWithQuotesFromSimpleDocument() throws Exception {
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

  private void assertDocumentView(DocumentView view, int width, int height,
      final boolean cacheUsed) {
    assertThat(view, notNullValue());
    int nbFilesAtTempRoot = cacheUsed ? 2 : 1;
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(nbFilesAtTempRoot));
    assertThat(view.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getName(), endsWith("file.swf"));
    assertThat(view.getWidth(), is(String.valueOf(width)));
    assertThat(view.getHeight(), is(String.valueOf(height)));
    assertThat(view.isDocumentSplit(), is(false));
    assertThat(view.areSearchDataComputed(), is(false));
  }
}
