package org.silverpeas.viewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.util.ImageUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-viewer.xml")
public class PreviewServiceTest {

  @Inject
  private PreviewService previewService;

  @After
  public void tearDown() throws Exception {
    FileUtils.cleanDirectory(new File(FileRepositoryManager.getTemporaryPath()));
  }

  @Test
  public void testOdtFile() throws Exception {
    final Preview preview = previewService.getPreview("file.odt", getDocumentNamed("file.odt"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testDocFile() throws Exception {
    final Preview preview = previewService.getPreview("file.doc", getDocumentNamed("file.doc"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testDocxFile() throws Exception {
    final Preview preview = previewService.getPreview("file.docx", getDocumentNamed("file.docx"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testOdpFile() throws Exception {
    final Preview preview = previewService.getPreview("file.odp", getDocumentNamed("file.odp"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("720"));
    assertThat(previewSize[1], is("540"));
  }

  @Test
  public void testPptFile() throws Exception {
    final Preview preview = previewService.getPreview("file.ppt", getDocumentNamed("file.ppt"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("720"));
    assertThat(previewSize[1], is("540"));
  }

  @Test
  public void testPptxFile() throws Exception {
    final Preview preview = previewService.getPreview("file.pptx", getDocumentNamed("file.pptx"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("720"));
    assertThat(previewSize[1], is("540"));
  }

  @Test
  public void testOdsFile() throws Exception {
    final Preview preview = previewService.getPreview("file.ods", getDocumentNamed("file.ods"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testXlsFile() throws Exception {
    final Preview preview = previewService.getPreview("file.xls", getDocumentNamed("file.xls"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  @Test
  public void testXlsxFile() throws Exception {
    final Preview preview = previewService.getPreview("file.clsx", getDocumentNamed("file.xlsx"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("612"));
    assertThat(previewSize[1], is("792"));
  }

  @Test
  public void testJpgFile() throws Exception {
    final Preview preview = previewService.getPreview("file.jpg", getDocumentNamed("file.jpg"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("1000"));
    assertThat(previewSize[1], is("750"));
  }

  @Test
  public void testJpegFile() throws Exception {
    // The uppercase letter of the following file extension is not a mistake
    final Preview preview = previewService.getPreview("file.jpEg", getDocumentNamed("file.jpEg"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("1000"));
    assertThat(previewSize[1], is("750"));
  }

  @Test
  public void testPdfFile() throws Exception {
    final Preview preview = previewService.getPreview("file.pdf", getDocumentNamed("file.pdf"));
    assertThat(preview, notNullValue());
    assertThat(preview.getPhysicalFile().getName().length(), greaterThan(10));
    final String[] previewSize = ImageUtil.getWidthAndHeight(preview.getPhysicalFile());
    assertThat(previewSize[0], is("595"));
    assertThat(previewSize[1], is("842"));
  }

  private File getDocumentNamed(final String name) throws Exception {
    final URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }
}
