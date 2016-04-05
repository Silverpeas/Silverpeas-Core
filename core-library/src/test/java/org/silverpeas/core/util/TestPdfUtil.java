/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.util;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.net.URL;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/07/13
 */
public class TestPdfUtil {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private final static String STAMP_FILE_NAME = "draft.png";
  private final static String WATERMARK_FILE_NAME = "draft.png";
  private final static String PDF_FILE_NAME = "pdfFile.pdf";
  private final static String RESULT_PDF_FILE_NAME = "pdfFile_out.pdf";

  private File rootTempPath;

  @Before
  public void beforeTest() throws Exception {
    rootTempPath = new File(FileRepositoryManager.getTemporaryPath());
    cleanTest();
  }

  @After
  public void afterTest() throws Exception {
    cleanTest();
  }

  /**
   * Cleaning files handled by a test
   */
  private void cleanTest() {
    if (rootTempPath.isDirectory()) {
      for (File file : listFiles(rootTempPath, null, false)) {
        deleteQuietly(file);
      }
    }
  }

  @Test(expected = RuntimeException.class)
  public void testStampWithDirectorySource() throws Exception {
    File pdfSource = getTestFile(STAMP_FILE_NAME).getParentFile();
    File stamp = getTestFile(STAMP_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    PdfUtil.stamp(pdfSource, stamp, pdfDestination);
  }

  @Test(expected = RuntimeException.class)
  public void testStampWithNonPdfSource() throws Exception {
    File pdfSource = getTestFile(STAMP_FILE_NAME);
    File stamp = getTestFile(STAMP_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    PdfUtil.stamp(pdfSource, stamp, pdfDestination);
  }

  @Test(expected = RuntimeException.class)
  public void testStampWithNullSource() throws Exception {
    File stamp = getTestFile(STAMP_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    PdfUtil.stamp(null, stamp, pdfDestination);
  }

  @Test(expected = RuntimeException.class)
  public void testStampWithNonImageFile() throws Exception {
    File pdfSource = getTestFile(PDF_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    PdfUtil.stamp(pdfSource, pdfSource, pdfDestination);
  }

  @Test
  public void testStamp() throws Exception {
    File pdfSource = getTestFile(PDF_FILE_NAME);
    long checksumSource = FileUtils.checksumCRC32(pdfSource);
    File stamp = getTestFile(STAMP_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    assertThat(pdfDestination.exists(), is(false));
    PdfUtil.stamp(pdfSource, stamp, pdfDestination);
    assertThat(FileUtils.checksumCRC32(pdfSource), is(checksumSource));
    assertThat(pdfDestination.exists(), is(true));
    assertThat(pdfDestination.length(), greaterThan(pdfSource.length()));
  }

  @Test
  public void testWatermark() throws Exception {
    File pdfSource = getTestFile(PDF_FILE_NAME);
    long checksumSource = FileUtils.checksumCRC32(pdfSource);
    File watermark = getTestFile(WATERMARK_FILE_NAME);
    File pdfDestination = FileUtils.getFile(rootTempPath, RESULT_PDF_FILE_NAME);
    assertThat(pdfDestination.exists(), is(false));
    PdfUtil.watermark(pdfSource, watermark, pdfDestination);
    assertThat(FileUtils.checksumCRC32(pdfSource), is(checksumSource));
    assertThat(pdfDestination.exists(), is(true));
    assertThat(pdfDestination.length(), greaterThan(pdfSource.length()));
  }

  /**
   * Get the file contained in resources of the test by its short name
   * @param name
   * @return
   * @throws Exception
   */
  private File getTestFile(final String name) throws Exception {
    final URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }
}
