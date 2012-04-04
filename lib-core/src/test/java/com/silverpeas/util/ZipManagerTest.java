/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author ehugonnet
 */
public class ZipManagerTest {

  public ZipManagerTest() {
  }

  @Before
  public void setUpClass() throws Exception {
    File tempDir = new File(PathTestUtil.TARGET_DIR + "temp");
    tempDir.mkdirs();
  }

  @After
  public void tearDownClass() throws Exception {
    File tempDir = new File(PathTestUtil.TARGET_DIR + "temp");
    FileUtils.forceDelete(tempDir);
  }

  /**
   * Test of compressPathToZip method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testCompressPathToZip() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "ZipSample";
    String outfilename =
      PathTestUtil.TARGET_DIR + "temp" + File.separatorChar + "testCompressPathToZip.zip";
    ZipManager.compressPathToZip(path, outfilename);
    ZipFile zipFile = new ZipFile(new File(outfilename), "UTF-8");
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.getEntries();
      int nbEntries = 0;
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        nbEntries++;
      }
      assertEquals(5, nbEntries);
      assertNotNull(zipFile.getEntry("ZipSample/simple.txt"));
      assertNotNull(zipFile.getEntry("ZipSample/level1/simple.txt"));
      assertNotNull(zipFile.getEntry("ZipSample/level1/level2b/simple.txt"));
      assertNotNull(zipFile.getEntry("ZipSample/level1/level2a/simple.txt"));
      assertNotNull(zipFile.getEntry("ZipSample/level1/level2a/"
        + new String("sïmplifié.txt".getBytes("UTF-8"), Charset.defaultCharset())));
      assertNull(zipFile.getEntry("ZipSample/level1/level2c/"));
    } finally {
      zipFile.close();
    }
  }

  /**
   * Test of compressStreamToZip method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testCompressStreamToZip() throws Exception {
    InputStream inputStream =
      this.getClass().getClassLoader().getResourceAsStream("FrenchScrum.odp");
    String filePathNameToCreate =
      File.separatorChar + "dir1" + File.separatorChar + "dir2" + File.separatorChar
      + "FrenchScrum.odp";
    String outfilename = PathTestUtil.TARGET_DIR + "temp" + File.separatorChar
      + "testCompressStreamToZip.zip";
    ZipManager.compressStreamToZip(inputStream, filePathNameToCreate, outfilename);
    inputStream.close();
    File file = new File(outfilename);
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.isFile());
    int result = ZipManager.getNbFiles(new File(outfilename));
    assertEquals(1, result);
    ZipFile zipFile = new ZipFile(file);
    assertNotNull(zipFile.getEntry(File.separatorChar + "dir1" + File.separatorChar + "dir2"
      + File.separatorChar + "FrenchScrum.odp"));
    zipFile.close();
  }

  /**
   * Test of extract method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testExtract() throws Exception {
    File source =
      new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "testExtract.zip");
    File dest =
      new File(PathTestUtil.TARGET_DIR + "temp" + File.separatorChar + "extract");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertNotNull(dest);
  }

  /**
   * Test of extract method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testExtractTargz() throws Exception {
    File source =
      new File(
      PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "testExtract.tar.gz");
    File dest = new File(PathTestUtil.TARGET_DIR + "temp" + File.separatorChar + "extract-tar");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertNotNull(dest);
    File uncompressedDir = new File(dest, "ZipSample");
    assertThat(uncompressedDir.exists(), is(true));
    assertThat(uncompressedDir.isDirectory(), is(true));
    assertThat(uncompressedDir.list().length, is(2));
  }

  /**
   * Test of extract method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testExtractTarBz2() throws Exception {
    File source = new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar
      + "testExtract.tar.bz2");
    File dest = new File(PathTestUtil.TARGET_DIR + "temp" + File.separatorChar + "extract-bz2");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertNotNull(dest);
    File uncompressedDir = new File(dest, "ZipSample");
    assertThat(uncompressedDir.exists(), is(true));
    assertThat(uncompressedDir.isDirectory(), is(true));
    assertThat(uncompressedDir.list().length, is(2));
  }

  /**
   * Test of getNbFiles method, of class ZipManager.
   *
   * @throws Exception
   */
  @Test
  public void testGetNbFiles() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "ZipSample";
    String outfilename = PathTestUtil.TARGET_DIR + "temp" + File.separatorChar
      + "testGetNbFiles.zip";
    ZipManager.compressPathToZip(path, outfilename);
    File file = new File(outfilename);
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.isFile());
    int result = ZipManager.getNbFiles(file);
    assertEquals(5, result);
  }
}
