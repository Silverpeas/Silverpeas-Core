/**
* Copyright (C) 2000 - 2012 Silverpeas
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU Affero General Public License as published by the Free Software Foundation, either version 3
* of the License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
* redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
* applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
* text describing the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.apache.commons.lang3.CharEncoding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static java.io.File.separatorChar;

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
    tempDir.mkdirs();
  }

  /**
* Test of compressPathToZip method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testCompressPathToZip() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + separatorChar + "ZipSample";
    String outfilename =
      PathTestUtil.TARGET_DIR + "temp" + separatorChar + "testCompressPathToZip.zip";
    ZipManager.compressPathToZip(path, outfilename);
    ZipFile zipFile = new ZipFile(new File(outfilename), CharEncoding.UTF_8);
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.getEntries();
      assertThat(zipFile.getEncoding(), is(CharEncoding.UTF_8));
      int nbEntries = 0;
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        nbEntries++;
      }
      assertThat(nbEntries, is(5));
      assertThat(zipFile.getEntry("ZipSample/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2b/simple.txt"), is(notNullValue()));     
      assertThat(zipFile.getEntry("ZipSample/level1/level2a/simple.txt"), is(notNullValue()));
     
      ZipEntry accentuatedEntry = zipFile.getEntry("ZipSample/level1/level2a/s\u00efmplifi\u00e9.txt") ;
      if(accentuatedEntry == null) {
       accentuatedEntry = zipFile.getEntry("ZipSample/level1/level2a/" + new String(
           "sïmplifié.txt".getBytes("UTF-8"), Charset.defaultCharset()));
      }
      assertThat(accentuatedEntry, is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2c/"), is(nullValue()));
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
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("FrenchScrum.odp");
    String filePathNameToCreate = separatorChar + "dir1" + separatorChar + "dir2" + separatorChar
        + "FrenchScrum.odp";
    String outfilename = PathTestUtil.TARGET_DIR + "temp" + separatorChar 
        + "testCompressStreamToZip.zip";
    ZipManager.compressStreamToZip(inputStream, filePathNameToCreate, outfilename);
    inputStream.close();
    File file = new File(outfilename);
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    int result = ZipManager.getNbFiles(new File(outfilename));
    assertThat(result, is(1));
    ZipFile zipFile = new ZipFile(file);
    assertThat(zipFile.getEntry("/dir1/dir2/FrenchScrum.odp"), is(notNullValue()));
    zipFile.close();
  }

  /**
* Test of extract method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testExtract() throws Exception {
    File source = new File(PathTestUtil.TARGET_DIR + "test-classes" + separatorChar 
        + "testExtract.zip");
    File dest = new File(PathTestUtil.TARGET_DIR + "temp" + separatorChar + "extract");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertThat(dest, is(notNullValue()));
  }

  /**
* Test of extract method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testExtractTargz() throws Exception {
    File source = new File(
      PathTestUtil.TARGET_DIR + "test-classes" + separatorChar + "testExtract.tar.gz");
    File dest = new File(PathTestUtil.TARGET_DIR + "temp" + separatorChar + "extract-tar");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertThat(dest, is(notNullValue()));
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
    File source = new File(PathTestUtil.TARGET_DIR + "test-classes" + separatorChar
      + "testExtract.tar.bz2");
    File dest = new File(PathTestUtil.TARGET_DIR + "temp" + separatorChar + "extract-bz2");
    dest.mkdirs();
    ZipManager.extract(source, dest);
    assertThat(dest, is(notNullValue()));
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
    String path = PathTestUtil.TARGET_DIR + "test-classes" + separatorChar + "ZipSample";
    String outfilename = PathTestUtil.TARGET_DIR + "temp" + separatorChar
      + "testGetNbFiles.zip";
    ZipManager.compressPathToZip(path, outfilename);
    File file = new File(outfilename);
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    int result = ZipManager.getNbFiles(file);
    assertThat(result, is(5));
  }
}
